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

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;

import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import netlib.PeerInfo;

public class P2PChat extends JFrame
{
	private Peer peer;

	private final DefaultListModel peerListModel;
	private final DefaultListModel chatParticipantsModel;

	private String centralHost;
	private int centralPort;

	private boolean hasPublishedSelf;

	private final VoiceChatHandler voiceHandler = new VoiceChatHandler();

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

		DefaultCaret caret = (DefaultCaret) chatTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		chatTextArea.setLineWrap(true);
		chatTextArea.append(
			"Press T to toggle voice transmission\n" +
			"Press F1 to modify microphone/speakers settings\n" +
			"Holding CTRL along with F1 will force modification\n"
		);

		KeyEventDispatcher toggleVoiceDispatcher = new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_T) {
					voiceHandler.toggleCapture();
				} else if (e.getKeyCode() == KeyEvent.VK_F1) {
					if ((e.getModifiers() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK
						|| !voiceHandler.isSatisified()) {
						try {
							Map map = VoiceChatHandler.getSourcesAvailable();
							String sources[] = (String[]) map.keySet().toArray(new String[0]);

							String s = (String) JOptionPane.showInputDialog(
								null,
								"Choose Input device",
								"Input device",
								JOptionPane.PLAIN_MESSAGE,
								null,
								sources,
								sources[0]
							);

							if (s != null && s.length() > 0)
								voiceHandler.setInput((Line) map.get(s));

							map = VoiceChatHandler.getTargetsAvailable();
							String targets[] = (String[]) map.keySet().toArray(new String[0]);

							s = (String) JOptionPane.showInputDialog(
								null,
								"Choose Output device",
								"Output device",
								JOptionPane.PLAIN_MESSAGE,
								null,
								targets,
								targets[0]
							);

							if (s != null && s.length() > 0)
								voiceHandler.setOutput((Line) map.get(s));
						} catch (LineUnavailableException ex) {
							JOptionPane.showMessageDialog(null, "Unable to acquire device!");
							ex.printStackTrace();
						}
					}
					return true;
				}
				return false;
			}
		};

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addKeyEventDispatcher(toggleVoiceDispatcher);
	}

	public static P2PChat get()
	{
		return instance;
	}

	public void setCentralInfo(String host, int port)
	{
		centralHost = host;
		centralPort = port;

		hasPublishedSelf = peer.publishSelf(host, port);
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {
		JScrollPane jScrollPane1 = new JScrollPane();
		JScrollPane jScrollPane4 = new JScrollPane();
		JScrollPane jScrollPane3 = new JScrollPane();

		JButton findPeersButton = new JButton("Find peers");
		JButton sendButton = new JButton("Send");

		chatParticipants = new JList();
		peerList = new JList();

		chatTextArea = new JTextArea();
		chatTextField = new JTextField();
		chatTextArea.setEditable(false);
		chatTextArea.setColumns(20);
		chatTextArea.setRows(5);
		jScrollPane1.setViewportView(chatTextArea);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		chatTextField.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER)
					sendTextMessage();
			}
		});

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
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				if (evt.isPopupTrigger())
					chatParticipantsPopup.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		});
		jScrollPane3.setViewportView(chatParticipants);

		peerList.setModel(peerListModel);
		peerList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				peerListMouseClicked(evt);
			}
			public void mouseReleased(java.awt.event.MouseEvent evt) {
				if (evt.isPopupTrigger())
					peerListPopup.show(evt.getComponent(), evt.getX(), evt.getY());
			}
		});
		jScrollPane4.setViewportView(peerList);

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

		peerListPopup = new JPopupMenu("Action...");
		JMenuItem mItemDisconnect = new JMenuItem("Disconnect");
		mItemDisconnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String peerInfo = (String) peerList.getSelectedValue();
				if (peerInfo == null)
					return;

				String peerHost = peerInfo.substring(0, peerInfo.indexOf(":"));
				if (peer.disconnectFrom(peerHost))
					chatTextArea.append("<Network> Successfully disconnected from: " + peerHost + "\n");
			}
		});

		JMenuItem mItemConnect = new JMenuItem("Connect");
		mItemConnect.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String hostName = JOptionPane.showInputDialog("Hostname/IP:");
				String portName = JOptionPane.showInputDialog("Port:");

				int port;
				try {
					port = Integer.parseInt(portName);
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "Invalid port name " + portName + "!");
					return;
				}

				try {
					peer.connect(hostName, port);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null, "Unable to establish a connection to " + hostName + ":" + portName + "!");
				}

				peerListModel.addElement(hostName + ":" + port);
			}
		});
		peerListPopup.add(mItemConnect);
		peerListPopup.add(mItemDisconnect);

		chatParticipantsPopup = new JPopupMenu("Action...");
		JMenuItem mItemKick = new JMenuItem("Kick");
		mItemKick.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				String nickName = (String) chatParticipants.getSelectedValue();
				if (nickName != null)
					peer.kick(nickName);
			}
		});
		chatParticipantsPopup.add(mItemKick);
		pack();
	}

	private void sendTextMessage()
	{
		String message = chatTextField.getText();
		if ("".equals(message))
			return;

		if (message.charAt(0) == '/') {
			String[] splitted = message.split(" ");
			if (splitted.length > 1) {
				if (splitted[0].equals("/nick")) {
					// A Nickname can contain spaces
					String newNick = new String();
					for (int i = 1; i < splitted.length; ++i)
						newNick += splitted[i] + " ";

					peer.setName(newNick);
					chatTextField.setText("");
					chatTextArea.append("You changed your name to " + newNick + ".");
				} else if (splitted[0].equals("/kick")) {
					// A Nickname can contain spaces
					String nick = new String();
					for (int i = 1; i < splitted.length; ++i)
						nick += splitted[i] + " ";

					if (peerListModel.contains(nick))
						peer.kick(nick);
				} else
					chatTextArea.append("Invalid command.");
			} else if (splitted[0].equals("/help")) {
				chatTextArea.append("Commands available:\n" +
					"/nick <new nickname> (Can contain spaces)\n" +
					"/kick <nickname> (Can contain spaces)\n"
				);
			}

			return;
		}

		String selected = (String) chatParticipants.getSelectedValue();
		if (selected != null)
			peer.sendMessage(message, selected);
		else
			peer.sendMessage(message, (Peer) null);

		chatTextField.setText("");
		chatTextArea.append("<" + peer.peerName + "> " + message + "\n");
	}

	private void findPeersButtonActionPerformed(java.awt.event.ActionEvent evt)
	{
		if (!hasPublishedSelf)
			hasPublishedSelf = peer.publishSelf(centralHost, centralPort);

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

	private void peerListMouseClicked(java.awt.event.MouseEvent evt)
	{
		if (SwingUtilities.isLeftMouseButton(evt)) {
			String peerInfo = (String) peerList.getSelectedValue();
			if (peerInfo == null)
				return;

			int sep = peerInfo.indexOf(":");
			String peerHost = peerInfo.substring(0, sep);
			int peerPort = Integer.parseInt(peerInfo.substring(sep + 1, peerInfo.length()));

			try {
				peer.connect(peerHost, peerPort);
			} catch (IOException e) {
				chatTextArea.append("Unable to connect to: " + peerInfo + "\n");
				e.printStackTrace();
			}
		} else if (evt.isPopupTrigger())
			peerListPopup.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	private void chatParticipantsMouseClicked(java.awt.event.MouseEvent evt)
	{
		String selected = (String) chatParticipants.getSelectedValue();
		if (selected != null)
			chatTextArea.append("You're now private messaging " + selected + " (CTRL+LCLICK to unselect)\n");
		else
			chatTextArea.append("You're now broadcasting to everyone.\n");

		if (evt.isPopupTrigger())
			chatParticipantsPopup.show(evt.getComponent(), evt.getX(), evt.getY());
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

	public void peerDisconnected(Peer node, boolean timeout)
	{
		int idx = chatParticipantsModel.indexOf(node.peerName);
		if (idx != -1)
			chatParticipantsModel.remove(idx);

		if (!timeout)
			chatTextArea.append(node.peerName + " has disconnected.\n");
		else
			chatTextArea.append(node.peerName + " has timed out.\n");
	}

	public void peerNameChanged(Peer node, String oldName, String newName)
	{
		int index = chatParticipantsModel.indexOf(oldName);
		if (index != -1) {
			if (peer.isChild(node)) {
				while (chatParticipantsModel.contains(newName) || newName.equals(peer.peerName)) {
					newName += "_";
					node.peerName = newName;
					peer.sendNameChangeRequest(node);
				}
			}

			chatParticipantsModel.setElementAt(newName, index);
			chatTextArea.append(oldName + " has changed name to " + newName + "\n");
		} else {
			System.out.println("Unable to find peer name " + oldName + " (" + newName + ")");
			chatParticipantsModel.addElement(newName);
		}
	}

	public void peerAcked(String from, String hostName, int port)
	{
		if (!peerListModel.contains(hostName + ":" + port)) {
			chatTextArea.append("New Peer Acked from " + from + ": " + hostName + ":" + port + "\n");
			peerListModel.addElement(hostName + ":" + port);
		}
	}

	public void centralConnectionFailed()
	{
		chatTextArea.append("Unable to establish a connection to the central server.\n");
	}

	public void transmitVoice(byte[] data, int count)
	{
		peer.sendAudioData(data, count);
	}

	public void peerTalk(byte[] data, int count)
	{
		voiceHandler.feedData(data, count);
	}

	private JList chatParticipants;
	private JTextArea chatTextArea;
	private JTextField chatTextField;
	private JList peerList;

	private JPopupMenu chatParticipantsPopup;
	private JPopupMenu peerListPopup;
}
