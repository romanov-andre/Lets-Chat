package networkchatclient;

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

public class ClientLogin extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField fieldUsername, fieldAddress, fieldPort;
	private JLabel errorMessage;

	private void connect(String name, String address, int port) {
		dispose();
		new ClientChat(name, address, port);
	}

	private void createWindow() {
		contentPane = new JPanel();
		setTitle("Let's Chat - Client");
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
		int y = 85;
		int width = 290;
		int height = 35;

		// Adds usersname label and text field
		JLabel lblUsername = new JLabel("Username: ");
		lblUsername.setBounds(20, y + 10, 180, 20);
		lblUsername.setFont(label);
		contentPane.add(lblUsername);

		fieldUsername = new JTextField();
		fieldUsername.setBounds(x, y, width, height);
		fieldUsername.setFont(label);
		contentPane.add(fieldUsername);

		// Adds IP Address label and text field
		y += 60;
		JLabel lblAddress = new JLabel("IPv4 Address: ");
		lblAddress.setBounds(20, y + 10, 180, 20);
		lblAddress.setFont(label);
		contentPane.add(lblAddress);

		fieldAddress = new JTextField();
		fieldAddress.setBounds(x, y, width, height);
		fieldAddress.setFont(label);
		fieldAddress.setToolTipText("IPv4 Address of the device where the server is launched.");
		// start -> cmd -> ipconfig -> " + "Your IPv4 is below Wireless LAN adapter
		// Wi-Fi.
		contentPane.add(fieldAddress);

		// Sets port label and field
		y += 60;
		JLabel lblPort = new JLabel("Port: ");
		lblPort.setBounds(20, y + 10, 180, 20);
		lblPort.setFont(label);
		contentPane.add(lblPort);

		fieldPort = new JTextField();
		fieldPort.setBounds(x, y, width, height);
		fieldPort.setFont(label);
		fieldPort.setToolTipText("Port number on which server is launched.");
		contentPane.add(fieldPort);

		// Sets an Error Message label
		errorMessage = new JLabel();
		errorMessage.setBounds(20, 270, 300, 20);
		errorMessage.setFont(label);
		errorMessage.setForeground(Color.red);
		errorMessage.setVisible(false);
		contentPane.add(errorMessage);

		// Adds Connect button
		y += 65;
		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(x + 175, y, 115, 30);
		btnConnect.setFont(label);
		contentPane.add(btnConnect);

		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = fieldUsername.getText();
				if (name.equals("")) {
					errorMessage.setText("Please enter a name");
					errorMessage.setVisible(true);
					return;
				} 
				String address = fieldAddress.getText();
				if (address.equals("")) {
					errorMessage.setText("Please enter an address");
					errorMessage.setVisible(true);
					return;
				}
				try {
					int port = Integer.parseInt(fieldPort.getText());
					connect(name, address, port);
				} catch (NumberFormatException ex) {
					errorMessage.setText("Please enter a valid port");
					errorMessage.setVisible(true);
				}
			}
		});
	}

	public static void main(String[] args) {
		ClientLogin login = new ClientLogin();
		login.createWindow();

	}
}