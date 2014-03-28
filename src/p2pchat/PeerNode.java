/*
 * P2PChat - Peer-to-Peer Chat Application
 *
 * Copyright (c) 2014 Ahmed Samy  <f.fallen45@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package p2pchat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import netlib.AsyncCallbacks;
import netlib.Connection;
import netlib.PeerInfo;
import netlib.Server;

public class PeerNode implements AsyncCallbacks
{
	private Server server;
	private Connection conn;

	private PeerNode child;
	private PeerNode parent;

	private int port;

	public PeerNode(PeerNode parent)
	{
		this.parent = parent;
		this.child  = child;

		this.conn   = null;
		this.server = null;
	}

	public PeerNode(PeerNode parent, int port) throws IOException
	{
		this.parent = parent;
		this.child  = null;
		this.conn   = null;
		this.port   = port;

		server = new Server(null, port, this);
		new Thread(server).start();
	}

	public PeerNode getChild()
	{
		return child;
	}
	public PeerNode getParent()
	{
		return parent;
	}

	public void connect(String host, int port) throws IOException
	{
		conn = new Connection(InetAddress.getByName(host), port, this);
		new Thread(conn).start();
	}

	/*
	 * The following function attempts to get a list
	 * of available peers from the central point.  See
	 * HybridCentralPoint.java for more information.
	 *
	 * If this function fails to connect or find any peers,
	 * a null is returned.
	*/
	public List discoverPeers(String host, int port)
	{
		try {
			Socket s = new Socket(host, port);
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			out.writeByte(0x1B);
			out.writeInt(this.port);
			out.writeByte(0x1A);

			DataInputStream in = new DataInputStream(s.getInputStream());
			int nr_peers = in.readInt();
			if (nr_peers <= 0)
				return null;

			List peers = new LinkedList();
			for (int i = 0; i < nr_peers; ++i) {
				byte[] peerAddress = new byte[4];
				in.read(peerAddress);

				String peerHost = InetAddress.getByAddress(peerAddress).getHostName();
				int peerPort = in.readInt();

				PeerInfo info = new PeerInfo();
				info.port = peerPort;
				info.host = peerHost;

				peers.add(info);
			}

			s.close();
			return peers;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public void sendMessage(String message)
	{
		ByteBuffer buffer = ByteBuffer.allocate(message.length() + 1);
		buffer.put((byte)0x1A);
		buffer.put(message.getBytes(Charset.forName("UTF-8")));

		for (PeerNode peer = child; peer != null; peer = peer.child)
			server.send(peer.conn.getChannel(), buffer.array());
	}

	@Override
	public boolean handleWrite(SocketChannel ch, int nr_wrote)
	{
		return true;
	}

	@Override
	public boolean handleRead(SocketChannel ch, ByteBuffer buffer, int nread)
	{
		byte request = buffer.get();

		if (request == 0x1A) {
			String str = new String(buffer.array(), Charset.forName("UTF-8"));
			return true;
		}

		return false;
	}

	@Override
	public boolean handleConnection(SocketChannel ch)
	{
		PeerNode peer = new PeerNode(this);
		try {
			peer.conn = new Connection(ch);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
}
