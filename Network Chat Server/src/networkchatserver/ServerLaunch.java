package networkchatserver;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class ServerLaunch extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField fieldPort;

	private void createWindow() {
		contentPane = new JPanel();
		setTitle("Let's Chat - Server");
		setSize(590, 370);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		addComponents();
		setVisible(true);
	}

	private void addComponents() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Creates a title
		JLabel title = new JLabel("Let's Chat");
		title.setBounds(200, 30, 380, 30);
		title.setFont(new Font("Consolas", Font.PLAIN, 30));
		contentPane.add(title);

		// Sets font used by components
		Font label = new Font("Consolas", Font.PLAIN, 18);

		// Sets an offset for components to be relative to each other
		int x = 160;
		int y = 134;
		int width = 290;
		int height = 35;

		// Adds port label and text field
		JLabel lblUsername = new JLabel("Port: ");
		lblUsername.setBounds(80, y + 10, 180, 20);
		lblUsername.setFont(label);
		contentPane.add(lblUsername);

		fieldPort = new JTextField();
		fieldPort.setBounds(x, y, width, height);
		fieldPort.setFont(label);
		contentPane.add(fieldPort);

		JLabel lblPortDesc = new JLabel();
		lblPortDesc.setBounds(x, y + 20, width + 60, height + 60);
		lblPortDesc.setFont(label);
		lblPortDesc.setText("<html>Potentially available ports:<br>1025 to 65535.</html>");
		contentPane.add(lblPortDesc);

		// Adds Launch button
		y += 120;
		JButton btnHost = new JButton("Launch");
		btnHost.setBounds(x + 175, y, 115, 30);
		btnHost.setFont(label);
		contentPane.add(btnHost);

		// Shows error message
		JLabel errorMessage = new JLabel();
		errorMessage.setBounds(80, y, 200, 30);
		errorMessage.setForeground(Color.red);
		errorMessage.setFont(label);
		errorMessage.setVisible(false);
		contentPane.add(errorMessage);

		btnHost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int port = 0;
				try {
					port = Integer.parseInt(fieldPort.getText());
				} catch (NumberFormatException ex) {
					System.out.println();
					errorMessage.setText("Invalid port");
					errorMessage.setVisible(true);
					return;
				}
				Server server = new Server(port);
				if (server.openConnection()) {
					server.start();
					dispose();
				} else {
					errorMessage.setText("Port already in use");
					errorMessage.setVisible(true);
				}

			}
		});
	}

	public static void main(String[] args) {
		ServerLaunch server = new ServerLaunch();
		server.createWindow();
	}

}
