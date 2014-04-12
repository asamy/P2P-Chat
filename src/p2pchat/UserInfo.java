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
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.GroupLayout;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class UserInfo extends JFrame {
	private KeyEventDispatcher keyDispatcher;

	private JTextField centralHost;
	private JTextField centralPort;
	private JTextField hostField;
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JTextField nickField;
	private JTextField portField;
	private JButton startButton;

	public UserInfo() {
		initComponents();

		keyDispatcher = new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					showMainWindow();
					return true;
				}
				return false;
			}
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addKeyEventDispatcher(keyDispatcher);
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {
		jLabel1 = new JLabel();
		hostField = new JTextField();
		jLabel2 = new JLabel();
		portField = new JTextField();
		jLabel3 = new JLabel();
		nickField = new JTextField();
		startButton = new JButton();
		jLabel4 = new JLabel();
		centralHost = new JTextField();
		jLabel5 = new JLabel();
		centralPort = new JTextField();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		jLabel1.setText("Host/IP:");

		jLabel2.setText("Port:");

		portField.setText("9119");

		jLabel3.setText("Nick:");

		startButton.setText("Start");
		startButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				showMainWindow();
			}
		});

		jLabel4.setText("Central Host/IP:");

		centralHost.setText("localhost");

		jLabel5.setText("Central Port:");

		centralPort.setText("9118");

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addComponent(jLabel1, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
							.addComponent(jLabel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(jLabel3, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
							.addComponent(portField, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
							.addComponent(nickField, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
							.addComponent(hostField)))
					.addComponent(startButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
					.addComponent(jLabel4, GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
					.addComponent(centralHost)
					.addComponent(centralPort, GroupLayout.Alignment.TRAILING)
					.addComponent(jLabel5, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap())
		);

		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {nickField, portField});

		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jLabel1)
					.addComponent(hostField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(jLabel4))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jLabel2)
					.addComponent(portField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(centralHost, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(jLabel3)
					.addComponent(nickField, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
					.addComponent(jLabel5))
				.addGap(10, 10, 10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(startButton)
					.addComponent(centralPort, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
		);

		layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {hostField, nickField, portField, startButton});

		layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel3});

		pack();
	}

	private void showMainWindow() {
		int port;
		try {
			port = Integer.parseInt(portField.getText());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}

		String nick = nickField.getText();
		if ("Network".equals(nick))
			return;

		if ("".equals(nick))
			nick = "unnamed";
		P2PChat c = new P2PChat(nick, hostField.getText(), port);

		int centralp;
		try {
			centralp = Integer.parseInt(centralPort.getText());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}

		c.setCentralInfo(centralHost.getText(), centralp);
		c.setVisible(true);

		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.removeKeyEventDispatcher(keyDispatcher);
		setVisible(false);
	}

	public static void main(String args[])
	{
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
			         | UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(P2PChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new UserInfo().setVisible(true);
			}
		});
	}
}
