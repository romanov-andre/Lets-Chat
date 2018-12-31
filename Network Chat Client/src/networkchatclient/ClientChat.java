package networkchatclient;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

public class ClientChat extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextArea textHistory;

	private JTextField textMessage;

	private String name;
	private String address;
	private int port;

	private DatagramSocket socket; // establishes a sending / receiving point for packets
	private InetAddress ipAddress;

	private boolean running = false;
	private Thread run, receive, send;
	private OnlineUsers users;

	public ClientChat(String name, String address, int port) {
		this.name = name;
		this.address = address;
		this.port = port;

		boolean connection = hasConnected();
		if (connection) {
			start();
		}
	}

	private boolean hasConnected() {
		boolean connection = false;
		try {
			socket = new DatagramSocket();
			ipAddress = InetAddress.getByName(address);
			connection = true;
		} catch (SocketException e) {
			connection = false;
		} catch (UnknownHostException e) {
			connection = false;
		}
		return connection;
	}

	private void start() {
		users = new OnlineUsers();
		running = true;
		run = new Thread(this, "Chat");
		run.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		createWindow();
		addComponents();
		String connection = "/c/" + name;
		send(connection);
		console("Trying to connect to server @ " + ipAddress + " : " + port + " ...");
	}

	public void run() {
		receive();
	}

	private void receive() {
		receive = new Thread("Receive") {
			public void run() {
				while (running) {
					byte[] data = new byte[1024];
					DatagramPacket packet = new DatagramPacket(data, data.length);
					try {
						socket.receive(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}
					String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
					if (message.startsWith("/c/")) {
						message = message.split("/c/")[1];
						console(message);
						textMessage.setEditable(true);
					} else if (message.startsWith("/p/")) {
						send(("/p/" + name).getBytes());
					} else if (message.startsWith("/k/") || message.startsWith("/e/")) {
						message = message.split("/k/|/e/")[1];
						console(message);
						textMessage.setEditable(false);
						synchronized (socket) {
							socket.close();
							return;
						}
					} else if (message.startsWith("/u/")) {
						String u[] = message.split("/u/|/n/");
						u = Arrays.copyOfRange(u, 1, u.length);
						users.update(u);
					} else {
						console(message);
					}
				}
			}
		};
		receive.start();
	}

	public void send(final byte[] data) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
				try {
					if (!socket.isClosed())
						socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	public void console(String message) {
		if (message.equals(""))
			return;
		textHistory.append(message + "\n");
	}

	public void send(String message) {
		if (message.equals(""))
			return;
		send(message.getBytes());
		textMessage.setText("");
	}

	private void addComponents() {
		// Sets font used by components
		Font font = new Font("Consolas", Font.PLAIN, 18);

		// Adds texting history area
		textHistory = new JTextArea(19, 85);
		textHistory.setFont(font);
		textHistory.setEditable(false);
		contentPane.add(textHistory, BorderLayout.NORTH);

		// Adds scrolling
		JScrollPane scrollPane = new JScrollPane(textHistory);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// Adds message area
		textMessage = new JTextField(77);
		textMessage.setFont(font);
		textMessage.setEditable(false);
		contentPane.add(textMessage, BorderLayout.WEST);
		textMessage.requestFocusInWindow();
		textMessage.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (textMessage.getText().equals(""))
						return;
					send("/m/" + name + ": " + textMessage.getText());
				}
			}
		});

		// Adds a menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu file = new JMenu("file");
		file.setFont(font);
		menuBar.add(file);

		JMenuItem online = new JMenuItem("online Users");
		online.setFont(font);
		file.add(online);
		online.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				users.setVisible(true);
			}
		});

		JMenuItem exit = new JMenuItem("exit");
		exit.setFont(font);
		file.add(exit);
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textMessage.isEditable()) {
					String disconnect = "/d/" + name;
					send(disconnect);
				}
				System.exit(0);
			}
		});

		// Adds Send button
		JButton btnSend = new JButton("Send");
		btnSend.setFont(font);
		contentPane.add(btnSend, BorderLayout.EAST);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (textMessage.getText().equals(""))
					return;
				send("/m/" + name + ": " + textMessage.getText());
			}
		});
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (textMessage.isEditable()) {
					String disconnect = "/d/" + name;
					send(disconnect);
				}
			}
		});
	}

	private void createWindow() {
		contentPane = new JPanel();
		setTitle("Client");
		setSize(880, 550);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setContentPane(contentPane);
		setVisible(true);
	}

}
