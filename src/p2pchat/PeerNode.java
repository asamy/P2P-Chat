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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import netlib.AsyncCallbacks;
import netlib.Connection;
import netlib.PeerInfo;
import netlib.Server;

public class PeerNode implements AsyncCallbacks
{
	private Server server;
	private Connection conn;

	public String peerName;
	private int port;

	private List peerList = new LinkedList();

	public PeerNode(PeerNode parent)
	{
		this.conn   = null;
		this.server = null;

		if (parent != null)
			peerList.add(parent);
	}

	@SuppressWarnings("LeakingThisInConstructor")
	public PeerNode(PeerNode parent, String nick, String host, int port) throws IOException
	{
		this.conn   = null;
		this.port   = port;
		this.peerName = nick;

		if (parent != null)
			peerList.add(parent);

		this.server = new Server("".equals(host) ? null : InetAddress.getByName(host), port, this);
		new Thread(this.server).start();
	}

	public void connect(String host, int port) throws IOException
	{
		if (conn != null && conn.isConnected())
			return;

		conn = new Connection(InetAddress.getByName(host), port, this);
		new Thread(conn).start();
	}

	public boolean acknowledgeSelf(String host, int port)
	{
		try {
			try (Socket s = new Socket(host, port)) {
				DataOutputStream out = new DataOutputStream(s.getOutputStream());

				out.writeByte(0x1B);
				out.writeInt(this.port);

				s.close();
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			P2PChat.get().centralConnectionFailed();
		}

		return false;
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
			try (Socket s = new Socket(host, port)) {
				DataOutputStream out = new DataOutputStream(s.getOutputStream());
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

					if (peerHost.equals(server.getAddress().getHostName()))
						continue;

					PeerInfo peerInfo = new PeerInfo();
					peerInfo.port = peerPort;
					peerInfo.host = peerHost;
					
					peers.add(peerInfo);
				}

				return peers;
			}
		} catch (IOException e) {
			e.printStackTrace();
			P2PChat.get().centralConnectionFailed();
		}

		return null;
	}

	public void kick(String name)
	{
		Iterator it = peerList.iterator();
		while (it.hasNext()) {
			PeerNode node = (PeerNode) it.next();
			if (name.equals(node.peerName)) {
				P2PChat.get().peerDisconnected(node);
				server.close(node.conn.getChannel());
			}
		}
	}

	public void setName(String name)
	{
		if (!name.equals(peerName))
			sendName(name);
	}

	public void sendMessage(String message)
	{
		int len = message.length();
		if (len == 0)
			return;

		ByteBuffer out = ByteBuffer.allocate(len * 5);
		out.put((byte)0x1A);
		out.putInt(len);
		for (int i = 0; i < len; ++i)
			out.putChar(message.charAt(i));

		Iterator it = peerList.iterator();
		while (it.hasNext()) {
			PeerNode node = (PeerNode) it.next();
			server.send(node.conn.getChannel(), out.array());
		}
	}

	private void sendName(String newName)
	{
		if (newName == null)
			newName = peerName;

		int len = newName.length();
		if (len == 0)
			return;

		ByteBuffer out = ByteBuffer.allocate(len * 5);
		out.put((byte)0x1A);
		out.putInt(len);
		for (int i = 0; i < len; ++i)
			out.putChar(newName.charAt(i));

		Iterator it = peerList.iterator();
		while (it.hasNext()) {
			PeerNode node = (PeerNode) it.next();
			System.out.println("Sending name to " + node.peerName);
			server.send(node.conn.getChannel(), out.array());
		}

		this.peerName = newName;
	}

	@Override
	public boolean handleWrite(SocketChannel ch, int nr_wrote)
	{
		System.out.println("handleWrite(): Wrote " + nr_wrote + " bytes.");
		return true;
	}

	@Override
	public boolean handleRead(SocketChannel ch, ByteBuffer buffer, int nread)
	{
		while (buffer.hasRemaining()) {
			byte request = buffer.get();

			switch (request) {
			case 0x1A: {
				int len = buffer.getInt();
				char[] data = new char[len];

				for (int i = 0; i < len; ++i)
					data[i] = buffer.getChar();

				String sender = null;

				Iterator it = peerList.iterator();
				while (it.hasNext()) {
					PeerNode node = (PeerNode) it.next();
					if (node.conn.getChannel() == ch) {
						sender = node.peerName;
						break;
					}
				}

				P2PChat.get().appendText(sender, new String(data));
				break;
			} case 0x1B: {
				int len = buffer.getInt();
				char[] data = new char[len];

				for (int i = 0; i < len; ++i)
					data[i] = buffer.getChar();

				String name = new String(data);
				Iterator it = peerList.iterator();
				while (it.hasNext()) {
					PeerNode node = (PeerNode) it.next();
					if (node.conn.getChannel() == ch) {
						P2PChat.get().peerNameChanged(node, node.peerName, name);
						node.peerName = name;
						break;
					}
				}
				break;
			} default:
				break;
			}
		}

		return true;
	}

	@Override
	public boolean handleConnection(SocketChannel ch)
	{
		PeerNode peer = new PeerNode(this);
		peerList.add(peer);

		try {
			peer.conn = new Connection(ch, null);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		sendName(null);
		P2PChat.get().peerConnected(peer);
		return true;
	}

	@Override
	public boolean handleConnectionClose(SocketChannel ch)
	{
		Iterator it = peerList.iterator();
		while (it.hasNext()) {
			PeerNode node = (PeerNode) it.next();
			if (node.conn.getChannel() == ch)
				P2PChat.get().peerDisconnected(node);
		}

		return true;
	}
}
