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
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Connection implements Runnable
{
	private InetAddress hostAddress;
	private int port;
	private Selector selector;
	private SocketChannel channel;
	private AsyncCallbacks listener;
	private List changeRequests = new LinkedList();
	private List pendingData = new ArrayList();

	public Connection(InetAddress hostAddress, int port, AsyncCallbacks listener) throws IOException
	{
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.listener = listener;
		this.initiateConnection();
	}

	public Connection(SocketChannel ch) throws IOException
	{
		this.selector = initSelector();
		this.channel = ch;
		ch.configureBlocking(false);

		synchronized(this.changeRequests) {
			this.changeRequests.add(new ChangeRequest(ch, ChangeRequest.REGISTER, SelectionKey.OP_WRITE));
		}
	}

	private Selector initSelector() throws IOException
	{
		return SelectorProvider.provider().openSelector();
	}

	private SocketChannel initiateConnection() throws IOException
	{
		SocketChannel ch = SocketChannel.open();
		ch.configureBlocking(false);

		ch.connect(new InetSocketAddress(this.hostAddress, this.port));
		synchronized(this.changeRequests) {
			this.changeRequests.add(new ChangeRequest(ch, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT));
		}
		return ch;
	}

	public SocketChannel getChannel()
	{
		return channel;
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
							break;
						case ChangeRequest.REGISTER:
							change.socket.register(this.selector, change.ops);
							break;
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

					if (key.isConnectable())
						this.finishConnection(key);
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

	private void finishConnection(SelectionKey key) throws IOException
	{
		try {
			channel.finishConnect();
		} catch (IOException e) {
			key.cancel();
			return;
		}

		key.interestOps(SelectionKey.OP_WRITE);
		if (!listener.handleConnection(channel)) {
			channel.close();
			key.cancel();
		}
	}

	private void read(SelectionKey key) throws IOException
	{
		ByteBuffer readBuffer = ByteBuffer.allocate(1024);
		int readnr;
		try {
			readnr = channel.read(readBuffer);
		} catch (IOException e) {
			key.cancel();
			channel.close();
			return;
		}

		if (readnr == -1 || !listener.handleRead(channel, readBuffer, readnr)) {
			channel.close();
			key.cancel();
		}
	}

	private void write(SelectionKey key) throws IOException
	{
		int nr_wrote = 0;

		synchronized(this.pendingData) {
			while (!pendingData.isEmpty()) {
				ByteBuffer buf = (ByteBuffer) pendingData.get(0);
				channel.write(buf);

				nr_wrote += buf.remaining();
				if (buf.remaining() > 0)
					break;
				pendingData.remove(0);
			}

			if (pendingData.isEmpty())
				key.interestOps(SelectionKey.OP_READ);
		}

		if (!listener.handleWrite(channel, nr_wrote)) {
			channel.close();
			key.cancel();
		}
	}

	public void send(byte[] data) throws IOException
	{
		synchronized (this.pendingData) {
			this.pendingData.add(data);
		}
		this.selector.wakeup();
	}
}
