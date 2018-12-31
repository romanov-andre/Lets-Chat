package networkchatclient;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OnlineUsers extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextArea list;

	public OnlineUsers() {
		contentPane = new JPanel();
		setContentPane(contentPane);
		setTitle("Online Users");
		setSize(200, 320);
		setType(Type.UTILITY);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setResizable(false);

		// Sets font used by components
		Font font = new Font("Consolas", Font.PLAIN, 18);

		list = new JTextArea(12, 17);
		list.setEditable(false);
		list.setFont(font);
		contentPane.add(list, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(list);
		contentPane.add(scrollPane, BorderLayout.CENTER);

	}

	public void update(String[] users) {
		list.setText("");
		for (int i = 0; i < users.length; i++)
			list.append(users[i] + "\n");
	}

}
