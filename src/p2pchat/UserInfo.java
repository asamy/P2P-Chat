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

public class UserInfo extends javax.swing.JFrame {
	public UserInfo() {
		initComponents();
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {
		jLabel1 = new javax.swing.JLabel();
		hostField = new javax.swing.JTextField();
		jLabel2 = new javax.swing.JLabel();
		portField = new javax.swing.JTextField();
		jLabel3 = new javax.swing.JLabel();
		nickField = new javax.swing.JTextField();
		startButton = new javax.swing.JButton();
		jLabel4 = new javax.swing.JLabel();
		centralHost = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		centralPort = new javax.swing.JTextField();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		jLabel1.setText("Host/IP:");

		jLabel2.setText("Port:");

		portField.setText("9119");

		jLabel3.setText("Nick:");

		startButton.setText("Start");
		startButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				startButtonActionPerformed(evt);
			}
		});

		jLabel4.setText("Central Host/IP:");

		centralHost.setText("localhost");

		jLabel5.setText("Central Port:");

		centralPort.setText("9118");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
							.addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
							.addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
							.addComponent(portField, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
							.addComponent(nickField, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
							.addComponent(hostField)))
					.addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
					.addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
					.addComponent(centralHost)
					.addComponent(centralPort, javax.swing.GroupLayout.Alignment.TRAILING)
					.addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addContainerGap())
		);

		layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {nickField, portField});

		layout.setVerticalGroup(
			layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(jLabel1)
					.addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addComponent(jLabel4))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(jLabel2)
					.addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addComponent(centralHost, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(jLabel3)
					.addComponent(nickField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
					.addComponent(jLabel5))
				.addGap(10, 10, 10)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
					.addComponent(startButton)
					.addComponent(centralPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
		);

		layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {hostField, nickField, portField, startButton});

		layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel1, jLabel2, jLabel3});

		pack();
	}

	private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
		int port;
		try {
			port = Integer.parseInt(portField.getText());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return;
		}

		String nick = nickField.getText();
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
		setVisible(false);
	}

	public static void main(String args[])
	{
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
			         | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(P2PChat.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new UserInfo().setVisible(true);
			}
		});
	}

	private javax.swing.JTextField centralHost;
	private javax.swing.JTextField centralPort;
	private javax.swing.JTextField hostField;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JTextField nickField;
	private javax.swing.JTextField portField;
	private javax.swing.JButton startButton;
}
