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

import netlib.AsyncCallbacks;
import netlib.Connection;
import netlib.Server;

public class PeerNode implements AsyncCallbacks
{
	private Server server;
	private Connection conn;

	private PeerNode child;
	private PeerNode parent;

	public PeerNode(PeerNode parent, int port) throws IOException
	{
		this.parent = parent;
		this.child  = null;
		this.conn   = null;

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

	/*
	 * The following function creates a new Node and connects
	 * it to @parent if @parent is not null.
	*/
	static public PeerNode create(PeerNode parent, String host, int port) throws IOException
	{
		PeerNode node = new PeerNode(parent, 9119);
		node.connect(host, port);
		return node;
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
	public String[] discoverPeers(String host, int port)
	{
		try {
			Socket s = new Socket(host, port);
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			out.writeByte(0x1A);

			DataInputStream in = new DataInputStream(s.getInputStream());
			int nr_peers = in.readInt();
			if (nr_peers <= 0)
				return null;

			String[] peers = new String[nr_peers];
			for (int i = 0; i < nr_peers; ++i) {
				byte[] peerAddress = new byte[4];
				in.read(peerAddress);

				peers[i] = InetAddress.getByAddress(peerAddress).getHostAddress();
			}

			s.close();
			return peers;
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Shouldn't be reached but to make NetBeans shut the hell up.
		return null;
	}

	@Override
	public boolean handleWrite(SocketChannel ch, int nr_wrote)
	{
		return false;
	}

	@Override
	public boolean handleRead(SocketChannel ch, ByteBuffer buffer, int nread)
	{
		return false;
	}

	@Override
	public boolean handleConnection(SocketChannel ch)
	{
		return false;
	}
}
