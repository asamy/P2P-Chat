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
package centralpoint;

import netlib.AsyncCallbacks;
import netlib.Server;
import netlib.PeerInfo;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.nio.channels.SocketChannel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/*
 * This class simulates a peer-to-peer hybrid model.
 * However, this is not all of the model, this is just
 * the Central Point for which the peers would communicate
 * with to retrieve a list of available peers.
 *
 * Every peer has to acknowledge himself to this server.
 * Few notes on the bytes used:
 *		Once a peer has connected to this server, it must send:
 *		1. 0x1A to retrieve the peer list.
 *			The peer list is sent as follows:
 *				Integer - Number of available peers
 *				byte[4] - for each peer address
 *				Integer - Peer port
 *			So for example:
 *				3
 *				127.0.0.1		-> 9119
 *				192.168.1.1		-> 4841
 *				138.158.15.69	-> 5165
 *		2. 0x1B to acknowledge self.
 *			Integer - port
*/
public class HybridCentralPoint implements AsyncCallbacks
{
	private Server m_server;
	private List m_peers = new LinkedList();

	public HybridCentralPoint() throws IOException
	{
		m_server = new Server(null, 9118, this);
		new Thread(m_server).start();
	}

	@Override
	public boolean handleWrite(SocketChannel ch, int nr_wrote)
	{
		return true;
	}

	@Override
	public boolean handleRead(SocketChannel ch, ByteBuffer buf, int nread)
	{
		try {
			while (buf.hasRemaining()) {
				byte request = buf.get();

				switch (request) {
				case 0x1A: {
					ByteBuffer out = ByteBuffer.allocate(1024);
					out.putInt(m_peers.size());

					Iterator it = m_peers.iterator();
					while (it.hasNext()) {
						PeerInfo info = (PeerInfo) it.next();

						out.put(info.address.getAddress());
						out.putInt(info.port);
					}

					m_server.send(ch, out.array());
					break;
				} case 0x1B: {
					PeerInfo info = new PeerInfo();
					info.port = buf.getInt();
					info.address = ch.socket().getInetAddress();

					m_peers.add(info);
					break;
				} default:
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public boolean handleConnection(SocketChannel ch)
	{
		return true;
	}

	@Override
	public boolean handleConnectionClose(SocketChannel ch)
	{
		return true;
	}

	public static void main(String args[])
	{
		try {
			new HybridCentralPoint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
