package networkchatserver;

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
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Server extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextArea consoleHistory;
	private JTextField consoleMessage;

	private int port;
	private String address;
	private DatagramSocket socket;
	private boolean running = false;
	private Thread run, manage, receive, send;

	private ArrayList<ServerClient> clients = new ArrayList<ServerClient>();
	private ArrayList<String> clientResponse = new ArrayList<String>();
	private final int MAX_ATTEMPTS = 5;
	private boolean rawDataPacket = false;

	public Server(int port) {
		this.port = port;
	}

	public boolean openConnection() {
		boolean connection = false;
		try {
			socket = new DatagramSocket(port);
			connection = true;
		} catch (SocketException e) {
			connection = false;
		}
		return connection;
	}

	public void start() {
		running = true;
		run = new Thread(this, "Server");
		run.start();
		createWindow();
		addComponents();
		console("/t/Sever successfully launched.");
		try {
			address = InetAddress.getLocalHost().toString().split("/")[1];
			console("/t/IPv4 Address: " + address);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		console("/t/Port: " + port);
		console("/t/Provide the details above to your friends so they can\nconnect to your server.");
		console("/help");
	}

	public void run() {
		manageClients();
		receive();
	}

	private void manageClients() {
		manage = new Thread("Manage") {
			public void run() {
				while (running) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendToAll("/p/pinging clients");
					sendStatus();
					for (int i = 0; i < clients.size(); i++) {
						ServerClient client = clients.get(i);
						if (!clientResponse.contains(client.getName())) {
							if (client.pinged >= MAX_ATTEMPTS) {
								disconnect(client.getName(), false);
							} else {
								client.pinged++;
							}
						} else {
							clientResponse.remove(new String(client.getName()));
							client.pinged = 0;
						}
					}
				}
			}
		};
		manage.start();
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
					process(packet);
				}
			}
		};
		receive.start();
	}

	private void process(DatagramPacket packet) {
		String packetData = new String(packet.getData(), packet.getOffset(), packet.getLength());
		if (rawDataPacket)
			consoleHistory.append(packetData + "\n");
		// Connection Packet
		if (packetData.startsWith("/c/")) {
			String name = packetData.split("/c/")[1];
			int id = UniqueIdentifier.getIdentifier();
			sendToAll(name + " has joined the chat");
			ServerClient client = new ServerClient(name, packet.getAddress(), packet.getPort(), id);
			clients.add(client);
			console("/t/Client " + name + "(" + client.getID() + ") @ " + client.getAddress() + ":" + client.getPort()
					+ " connected.");
			send(("/c/" + "You have successfully connected to the server.").getBytes(), client.getAddress(),
					client.getPort());
		} else if (packetData.startsWith("/m/")) {
			String message = packetData.substring(3, packetData.length());
			sendToAll(message);
			consoleHistory.append(message + "\n");
		} else if (packetData.startsWith("/p/")) {
			String name = packetData.split("/p/")[1];
			clientResponse.add(name);
		} else if (packetData.startsWith("/d/")) {
			String name = packetData.split("/d/")[1];
			disconnect(name, true);
			sendToAll(name + " has left the chat");
		} else {
			console(packetData);
		}
	}

	private void sendStatus() {
		String users = "/u/";
		for (int i = 0; i < clients.size() - 1; i++) {
			users += clients.get(i).getName() + "/n/";
		}
		if (clients.size() > 0) {
			users += clients.get(clients.size() - 1).getName();
			sendToAll(users);
		}
	}

	private void sendToAll(String message) {
		for (int i = 0; i < clients.size(); i++) {
			ServerClient client = clients.get(i);
			send(message.getBytes(), client.getAddress(), client.getPort());
		}
	}

	private void send(final byte[] data, final InetAddress address, final int port) {
		send = new Thread("Send") {
			public void run() {
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		send.start();
	}

	private void console(String message) {
		if (message.equals(""))
			return;
		if (!message.startsWith("/")) {
			consoleMessage.setText("");
			return;
		} else if (message.startsWith("/raw")) {
			rawDataPacket = !rawDataPacket;
			if (rawDataPacket)
				message = "Raw Mode enabled.";
			else
				message = "Raw Mode disabled.";
		} else if (message.startsWith("/clients")) {
			message = clients.size() + " Connected Clients\n";
			for (int i = 0; i < clients.size(); i++) {
				ServerClient client = clients.get(i);
				message += client.getName() + "(" + client.getID() + ") " //
						+ client.getAddress() + " : " + client.getPort() + "\n";
			}
		} else if (message.startsWith("/kick") && message.length() > 5) {
			String name = message.substring(6, message.length());
			boolean exists = false;
			for (int i = 0; i < clients.size(); i++) {
				if (clients.get(i).getName().equals(name)) {
					send("/k/You have been kicked from the server.".getBytes(), //
							clients.get(i).getAddress(), clients.get(i).getPort());
					disconnect(name, true);
					sendToAll(name + " has been kicked from the chat");
					exists = true;
					break;
				}
			}
			if (!exists)
				console("/t/Client " + name + " is not connected.");
			return;
		} else if (message.startsWith("/help")) {
			message = "Available Commands: \n" + //
					"- - - - - - - - - - - - - - - - - - - - - -\n" + //
					"/raw - enables/disables viewing packets. \n" + //
					"/clients - displays all connected clients.\n" + //
					"/kick [name] - boots a client.\n" + //
					"/help - displays all commands.\n" + //
					"- - - - - - - - - - - - - - - - - - - - - -";
		} else if (message.startsWith("/t/")) {
			message = message.substring(3, message.length());
		} else if (message.startsWith("/")) {
			message = "Command Unrecognized.";
		}
		consoleHistory.append(message + "\n");
		consoleMessage.setText("");
	}

	private void disconnect(String name, boolean status) {
		ServerClient client = null;
		for (int i = 0; i < clients.size(); i++) {
			client = clients.get(i);
			if (client.getName().equals(name)) {
				clients.remove(client);
				break;
			}
		}

		if (status) {
			console("/t/Client " + name + "(" + client.getID() + ") @ " + client.getAddress() + ":" + client.getPort()
					+ " disconnected.");
		} else
			console("/t/Client " + name + "(" + client.getID() + ") @ " + client.getAddress() + ":" + client.getPort()
					+ " disconnected.");

	}

	private void addComponents() {
		// Sets font used by components
		Font font = new Font("Consolas", Font.PLAIN, 18);

		// Adds texting history area
		consoleHistory = new JTextArea(12, 55);
		consoleHistory.setFont(font);
		consoleHistory.setEditable(false);
		contentPane.add(consoleHistory, BorderLayout.NORTH);

		// Adds scrolling
		JScrollPane scrollPane = new JScrollPane(consoleHistory);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		// Adds message area
		consoleMessage = new JTextField(45);
		consoleMessage.setFont(font);
		contentPane.add(consoleMessage, BorderLayout.WEST);
		consoleMessage.requestFocusInWindow();
		consoleMessage.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					console(consoleMessage.getText());
				}
			}
		});

		// Adds Send button
		JButton btnSend = new JButton("Send");
		btnSend.setFont(font);
		contentPane.add(btnSend, BorderLayout.EAST);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				console(consoleMessage.getText());
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				sendToAll("/e/The server @ " + address + ":" + port + " disconnected.");
				System.exit(0);
			}
		});
	}

	private void createWindow() {
		contentPane = new JPanel();
		setTitle("Server");
		setSize(590, 370);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setResizable(false);
		setContentPane(contentPane);
		setVisible(true);
	}
}
