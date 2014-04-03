/*
 * P2PChat - Peer-to-Peer Chat Application
 *
 * Code taken from http://rox-xmlrpc.sourceforge.net/niotut/
 * Modified a little to fit.
 */
package netlib;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Server implements Runnable
{
	private InetAddress hostAddress;
	private int port;

	private Selector selector;
	private ServerSocketChannel channel;
	private NetEventListener listener;

	private final List changeRequests = new LinkedList();
	private final Map pendingData = new HashMap();

	public Server(InetAddress hostAddress, int port, NetEventListener listener) throws IOException
	{
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.listener = listener;
	}

	public InetAddress getAddress()
	{
		return hostAddress;
	}

	private Selector initSelector() throws IOException
	{
		Selector s = SelectorProvider.provider().openSelector();

		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(this.hostAddress, this.port));
		channel.register(s, SelectionKey.OP_ACCEPT);

		if (hostAddress == null)
			hostAddress = channel.socket().getInetAddress();

		return s;
	}

	public boolean hasChannel(SocketChannel ch)
	{
		return ch != null && ch.keyFor(selector) != null;
	}

	public void run()
	{
		while (true) {
			try {
				synchronized(changeRequests) {
					Iterator changes = changeRequests.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(selector);
							key.interestOps(change.ops);
						}
					}
					changeRequests.clear();
				}
				selector.select();

				Iterator selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey  key = (SelectionKey)selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid())
						continue;

					if (key.isAcceptable())
						accept(key);
					else if (key.isReadable())
						read(key);
					else if (key.isWritable())
						write(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void accept(SelectionKey key) throws IOException
	{
		ServerSocketChannel ch = (ServerSocketChannel) key.channel();
		SocketChannel s_ch = ch.accept();

		s_ch.configureBlocking(false);
		s_ch.register(selector, SelectionKey.OP_READ);
		if (!listener.handleConnection(s_ch)) {
			s_ch.close();
			key.cancel();
		}
	}

	private void read(SelectionKey key) throws IOException
	{
		SocketChannel ch = (SocketChannel) key.channel();

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int count;
		try {
			count = ch.read(buffer);
		} catch (IOException e) {
			close(ch);
			return;
		}

		buffer.flip();
		if (count == -1
			|| (listener != null && !listener.handleRead(ch, buffer, count)))
			close(ch);
	}

	private void write(SelectionKey key) throws IOException
	{
		SocketChannel ch = (SocketChannel) key.channel();
		int nr_wrote = 0;

		synchronized(pendingData) {
			List queue = (List) pendingData.get(ch);
			/**
			 * Null exception alert:
			 *		This can happen once a connection was established.
			 * Check send() for more information.
			 */
			if (queue == null)
				return;

			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				ch.write(buf);

				nr_wrote += buf.remaining();
				if (buf.remaining() > 0)
					break;
				queue.remove(0);
			}

			if (queue.isEmpty())
				key.interestOps(SelectionKey.OP_READ);
		}

		if (!listener.handleWrite(ch, nr_wrote))
			close(ch);
	}

	public void send(SocketChannel ch, byte[] data)
	{
		synchronized(changeRequests) {
			changeRequests.add(new ChangeRequest(ch, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			synchronized(pendingData) {
				List queue = (List) pendingData.get(ch);
				if (queue == null) {
					queue = new ArrayList();
					pendingData.put(ch, queue);
				}

				queue.add(ByteBuffer.wrap(data));
			}
		}

		selector.wakeup();
	}

	public void close(SocketChannel ch)
	{
		if (!listener.handleConnectionClose(ch))
			return;

		try {
			ch.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ch.keyFor(selector).cancel();
		synchronized(changeRequests) {
			Iterator changes = changeRequests.iterator();
			while (changes.hasNext()) {
				ChangeRequest req = (ChangeRequest) changes.next();
				if (req.socket == ch) {
					changeRequests.remove(req);
					break;
				}
			}
		}
	}
}
