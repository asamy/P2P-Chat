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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.text.DefaultCaret;

import netlib.PeerInfo;

public class P2PChat extends javax.swing.JFrame
{
	private Peer peer;
	private final DefaultListModel peerListModel, chatParticipantsModel;
	private String centralHost;
	private int centralPort;
	private boolean hasAckedSelf;

	private static P2PChat instance;

	@SuppressWarnings("LeakingThisInConstructor")
	public P2PChat(String nick, String host, int port)
	{
		try {
			peer = new Peer(null, nick, host, port);
		} catch (IOException e) {
			e.printStackTrace();
		}

		peerListModel = new DefaultListModel();
		chatParticipantsModel = new DefaultListModel();

		instance = this;
		initComponents();

		DefaultCaret caret = (DefaultCaret)chatTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		chatTextArea.setLineWrap(true);
	}

	public static P2PChat get()
	{
		return instance;
	}

	public void setCentralInfo(String host, int port)
	{
		centralHost = host;
		centralPort = port;

		hasAckedSelf = peer.acknowledgeSelf(host, port);
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {
		jScrollPane1 = new javax.swing.JScrollPane();
		chatTextArea = new javax.swing.JTextArea();
		chatTextField = new javax.swing.JTextField();
		findPeersButton = new javax.swing.JButton();
		jScrollPane3 = new javax.swing.JScrollPane();
		chatParticipants = new javax.swing.JList();
		jScrollPane4 = new javax.swing.JScrollPane();
		peerList = new javax.swing.JList();
		sendButton = new javax.swing.JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		chatTextArea.setEditable(false);
		chatTextArea.setColumns(20);
		chatTextArea.setRows(5);
		jScrollPane1.setViewportView(chatTextArea);

		chatTextField.setText("Type here...");
		chatTextField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER)
					sendTextMessage();
			}
		});

		findPeersButton.setText("Find Peers");
		findPeersButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				findPeersButtonActionPerformed(evt);
			}
		});

		chatParticipants.setModel(chatParticipantsModel);
		chatParticipants.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				chatParticipantsMouseClicked(evt);
			}
		});
		jScrollPane3.setViewportView(chatParticipants);

		peerList.setModel(peerListModel);
		peerList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				peerListMouseClicked(evt);
			}
		});
		jScrollPane4.setViewportView(peerList);

		sendButton.setText("Send");
		sendButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendTextMessage();
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
					.addComponent(chatTextField)
					.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
					.addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
					.addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(findPeersButton, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
					.addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
			.addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
					.addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 371, Short.MAX_VALUE)
					.addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
					.addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(findPeersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(chatTextField)
					.addComponent(sendButton)))
		);

		pack();
	}

	private void sendTextMessage()
	{
		String message = chatTextField.getText();
		if ("".equals(message))
			return;

		if (message.charAt(0) == '/') {
			String[] splitted = message.split(" ");
			if (splitted[0].equals("/nick") && splitted.length > 1) {
				// A Nickname can contain spaces
				String newNick = new String();
				for (int i = 1; i < splitted.length; ++i)
					newNick += splitted[i] + " ";
				peer.setName(newNick);
				chatTextField.setText("");
				return;
			}
		}

		peer.sendMessage(message);
		chatTextField.setText("");
		chatTextArea.append("<" + peer.peerName + "> " + message + "\n");
	}

	private void findPeersButtonActionPerformed(java.awt.event.ActionEvent evt)
	{
		if (!hasAckedSelf)
			hasAckedSelf = peer.acknowledgeSelf(centralHost, centralPort);

		List peers = peer.discoverPeers(centralHost, centralPort);
		if (peers == null) {
			chatTextArea.append("No peers were found.\n");
			return;
		}
		peerListModel.clear();

		Iterator it = peers.iterator();
		while (it.hasNext()) {
			PeerInfo info = (PeerInfo) it.next();
			peerListModel.addElement(info.host + ":" + info.port);
		}
	}

	private void peerListMouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getButton() == MouseEvent.BUTTON1) {
			String peerInfo = (String) peerList.getSelectedValue();
			if (peerInfo == null)
				return;

			String peerHost = peerInfo.substring(0, peerInfo.indexOf(":"));
			int peerPort = Integer.parseInt(peerInfo.substring(peerInfo.indexOf(":")+1, peerInfo.length()));

			try {
				peer.connect(peerHost, peerPort);
			} catch (IOException e) {
				chatTextArea.append("Unable to connect to: " + peerInfo + "\n");
				e.printStackTrace();
			}
		}
	}

	private void chatParticipantsMouseClicked(java.awt.event.MouseEvent evt) {
		if (evt.getButton() == MouseEvent.BUTTON2) {
			JMenu menu = new JMenu("Action...");

			if (!chatParticipantsModel.isEmpty()) {
				javax.swing.JButton buttonKick = new javax.swing.JButton("Kick");
				buttonKick.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						peer.kick((String) chatParticipants.getSelectedValue());
					}
				});

				menu.add(buttonKick);
			}

			// TODO, more buttons.
			menu.setVisible(true);
		}
	}

	public void appendText(String sender, String text)
	{
		if (sender == null)
			sender = "unknown";

		chatTextArea.append("<" + sender + "> " + text + "\n");
	}

	public void peerConnected(Peer newPeer)
	{
		if (newPeer.peerName == null)
			newPeer.peerName = "unnamed";

		chatTextArea.append(newPeer.peerName + " has connected.\n");
		chatParticipantsModel.addElement(newPeer.peerName);
	}

	public void peerDisconnected(Peer node)
	{
		node.sendMessage("You were kicked.");

		int idx = chatParticipantsModel.indexOf(node.peerName);
		if (idx != -1)
			chatParticipantsModel.remove(idx);

		chatTextArea.append(node.peerName + " was kicked from this chat.\n");
	}

	public void peerNameChanged(Peer node, String oldName, String newName)
	{
		int index = chatParticipantsModel.indexOf(oldName);
		if (index != -1) {
			chatParticipantsModel.setElementAt(newName, index);
			chatTextArea.append(oldName + " has changed name to " + newName + "\n");
		} else {
			System.out.println("Unable to find peer name " + oldName + " (" + newName + ")");
			chatParticipantsModel.addElement(newName);
		}
	}

	public void centralConnectionFailed()
	{
		chatTextArea.append("Unable to establish a connection to the central server.\n");
	}

	private javax.swing.JList chatParticipants;
	private javax.swing.JTextArea chatTextArea;
	private javax.swing.JTextField chatTextField;
	private javax.swing.JButton findPeersButton;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JScrollPane jScrollPane3;
	private javax.swing.JScrollPane jScrollPane4;
	private javax.swing.JList peerList;
	private javax.swing.JButton sendButton;
}
