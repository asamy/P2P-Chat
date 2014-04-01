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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import netlib.NetEventListener;
import netlib.Connection;
import netlib.PeerInfo;
import netlib.Server;

/**
 * Peer A:
 *		server open, null connection
 * Peer B:
 *		server open, connection open to A.
 * 
 * Peer A can send but not receive.
 * Peer B can receive but not send!
 */

public class Peer implements NetEventListener
{
	private final Server server;
	private Connection conn;
	private SocketChannel channel;

	public String peerName;
	private int port;

	private final List children = new LinkedList();

	public Peer(Peer parent)
	{
		conn   = null;
		server = null;

		if (parent != null)
			children.add(parent);
	}

	public Peer(Peer parent, String nick, String host, int port) throws IOException
	{
		conn   = null;
		peerName = nick;
		this.port   = port;

		if (parent != null)
			children.add(parent);

		server = new Server("".equals(host) ? null : InetAddress.getByName(host), port, this);
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
		Iterator it = children.iterator();
		while (it.hasNext()) {
			Peer node = (Peer) it.next();
			if (name.equals(node.peerName)) {
				P2PChat.get().peerDisconnected(node);
				server.close(node.channel);
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

		System.out.println("Send message: " + message);
		send(null, mkbuffer((byte)0x1A, message, len).array());
	}

	private void putString(ByteBuffer buffer, String str, int len)
	{
		buffer.putInt(len);
		for (int i = 0; i < len; ++i)
			buffer.putChar(str.charAt(i));
	}

	private String getString(ByteBuffer buffer)
	{
		int len = buffer.getInt();
		if (len == 0)
			return null;

		char[] data = new char[len];
		for (int i = 0; i < len; ++i)
			data[i] = buffer.getChar();

		return new String(data);
	}

	private ByteBuffer mkbuffer(byte request, String str, int len)
	{
		ByteBuffer out = ByteBuffer.allocate((len * 2) + 5);
		out.put(request);
		putString(out, str, len);

		return out;
	}

	private void sendName(String newName)
	{
		if (newName == null)
			newName = peerName;

		int len = newName.length();
		if (len == 0)
			return;

		ByteBuffer out = mkbuffer((byte)0x1B, newName, len);
		Iterator it = children.iterator();
		while (it.hasNext())
			sendName((Peer)it.next(), out);
		peerName = newName;
	}

	private void sendName(Peer node, ByteBuffer out)
	{
		if (node == null) {
			System.out.println("sendName() cannot send name to null peer!");
			return;
		}

		System.out.println("Sending name to " + node.peerName);
		send(node, out.array());
	}

	private void send(Peer node, byte[] data)
	{
		if (conn != null) {
			System.out.println("sending to connection");
			conn.send(data);
		}

		if (node == null) {
			System.out.println("Sending to every single peer");
			// General purpose sending
			for (Object o : children) {
				Peer n = (Peer) o;
				server.send(n.channel, data);
			}
		} else {
			System.out.println("sending to one peer.");
			server.send(node.channel, data);
		}
	}

	public boolean handleWrite(SocketChannel ch, int count)
	{
		System.out.println("handleWrite(): Wrote " + count + " bytes.");
		return true;
	}

	public boolean handleRead(SocketChannel ch, ByteBuffer buffer, int count)
	{
		while (buffer.hasRemaining()) {
			byte request = buffer.get();

			switch (request) {
			case 0x1A: {
				System.out.println("Receive peer message");
				String message = getString(buffer);
				String sender = null;

				Iterator it = children.iterator();
				while (it.hasNext()) {
					Peer node = (Peer) it.next();
					if (node.channel == ch) {
						sender = node.peerName;
						break;
					}
				}

				P2PChat.get().appendText(sender, message);
				break;
			} case 0x1B: {
				System.out.println("Receive peer name");
				String name = getString(buffer);
				Iterator it = children.iterator();
				while (it.hasNext()) {
					Peer node = (Peer) it.next();
					if (node.channel == ch) {
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

	public boolean handleConnection(SocketChannel ch)
	{
		Peer peer = new Peer(this);
		children.add(peer);

		peer.channel = ch;
		sendName(peer, mkbuffer((byte)0x1B, peerName, peerName.length()));
		P2PChat.get().peerConnected(peer);
		return true;
	}

	public boolean handleConnectionClose(SocketChannel ch)
	{
		Iterator it = children.iterator();
		while (it.hasNext()) {
			Peer node = (Peer) it.next();
			if (node.channel == ch) {
				P2PChat.get().peerDisconnected(node);
				children.remove(node);
				break;
			}
		}

		P2PChat.get().appendText("Network", "Unable to find disconnected peer!");
		return true;
	}
}
