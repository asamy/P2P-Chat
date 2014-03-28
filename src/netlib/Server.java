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
	private AsyncCallbacks listener;
	private List changeRequests = new LinkedList();
	private Map pendingData = new HashMap();

	public Server(InetAddress hostAddress, int port, AsyncCallbacks listener) throws IOException
	{
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.listener = listener;
	}

	private Selector initSelector() throws IOException
	{
		Selector s = SelectorProvider.provider().openSelector();

		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(this.hostAddress, this.port));
		channel.register(s, SelectionKey.OP_ACCEPT);
		return s;
	}

	public void run()
	{
		while (true) {
			try {
				synchronized(this.changeRequests) {
					Iterator changes = this.changeRequests.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(this.selector);
							key.interestOps(change.ops);
						}
					}
					this.changeRequests.clear();
				}
				this.selector.select();

				Iterator selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey  key = (SelectionKey)selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid())
						continue;

					if (key.isAcceptable())
						this.accept(key);
					else if (key.isReadable())
						this.read(key);
					else if (key.isWritable())
						this.write(key);
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
		if (listener.handleConnection(s_ch)) {
			s_ch.configureBlocking(false);
			s_ch.register(this.selector, SelectionKey.OP_READ);
		} else {
			s_ch.close();
			key.cancel();
		}
	}

	private void read(SelectionKey key)
	{
		SocketChannel ch = (SocketChannel)key.channel();

		ByteBuffer readBuffer = ByteBuffer.allocate(8192);
		int readnr;
		try {
			readnr = ch.read(readBuffer);
		} catch (IOException e) {
			try {
				ch.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				key.cancel();
			}
			return;
		}

		readBuffer.flip();
		if (readnr == -1 || !listener.handleRead(ch, readBuffer, readnr))
			close(ch);
	}

	private void write(SelectionKey key) throws IOException
	{
		SocketChannel ch = (SocketChannel) key.channel();
		int nr_wrote = 0;

		synchronized(this.pendingData) {
			List queue = (List) this.pendingData.get(ch);
			while (!queue.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				ch.write(buf);

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
		synchronized(this.changeRequests) {
			this.changeRequests.add(new ChangeRequest(ch, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			synchronized(this.pendingData) {
				List queue = (List) this.pendingData.get(ch);
				if (queue == null) {
					queue = new ArrayList();
					this.pendingData.put(ch, queue);
				}

				queue.add(ByteBuffer.wrap(data));
			}
		}

		this.selector.wakeup();
	}

	public void close(SocketChannel ch)
	{
		try {
			ch.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ch.keyFor(this.selector).cancel();
		synchronized(this.changeRequests) {
			Iterator changes = this.changeRequests.iterator();
			while (changes.hasNext()) {
				ChangeRequest req = (ChangeRequest) changes.next();
				if (req.socket == ch) {
					this.changeRequests.remove(req);
					break;
				}
			}
		}
	}
}
