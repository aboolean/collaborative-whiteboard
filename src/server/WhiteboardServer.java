package server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import data.*;

public class WhiteboardServer {
	private final List<User> users;
	private final List<MasterBoard> boards;

	private final ServerSocket serverSocket;

	/*
	 * Invariants: - boards is always sorted by ID number - users is always
	 * sorted by ID number
	 */

	/**
	 * Initializes a new WhiteboardServer operating on port 55000 from this
	 * machine.
	 * 
	 * @param listeningPort
	 *            The port accepting connections.
	 * @throws IOException
	 *             An occurred occurred over network connection.
	 */
	public WhiteboardServer(int listeningPort) throws IOException {
		// initialize users and boards
		// users = Collections.synchronizedList(new ArrayList<User>());
		users = new ArrayList<User>();
		boards = new ArrayList<MasterBoard>();

		serverSocket = new ServerSocket(listeningPort);
	}

	/**
	 * Returns the current IP address of the server. If none is available,
	 * return null.
	 * 
	 * @return the string representation of the IP address or null
	 */
	public String getIP() {
		try {
			serverSocket.getInetAddress();
			return InetAddress.getLocalHost()
					.getHostAddress();
		} catch (UnknownHostException e) {
			return null;
		}
	}

	/**
	 * Returns the port the server is listening on. If a port is not bound,
	 * returns null.
	 * 
	 * @return the string representation of the port number or null
	 */
	public String getPort() {
		int port = serverSocket.getLocalPort();
		if (port < 0) // not yet bound
			return null;
		return String.valueOf(port);
	}

	/**
	 * Returns the board corresponding to the provided boardID. If this board
	 * does not exist, returns null.
	 * 
	 * @param boardID
	 *            the identification number of a board
	 * @return the MasterBoard or null
	 */
	public MasterBoard fetchBoard(int boardID) {
		MasterBoard selectedBoard = null;
		synchronized (boards) {
			for (MasterBoard board : boards) {
				if (board.getID() == boardID) {
					selectedBoard = board;
					break;
				}
			}
		}
		return selectedBoard;
	}

	/**
	 * Removes expUser from the list of connected users. Called when the
	 * connection to the client has been terminated.
	 * 
	 * @param expUser
	 *            a disconnected User
	 */
	public void deleteUser(User expUser) {
		synchronized (users) {
			users.remove(expUser);
		}
	}

	/**
	 * If the boardID is found, removes the specified board from the list of
	 * available board and calls terminateBoard on the removed board. No other
	 * boards can be added or removed until this method returns.
	 * 
	 * @param boardID
	 *            the identification number of an active MasterBoard
	 */
	public void deleteBoard(int boardID) {
		synchronized (boards) {
			for (MasterBoard board : boards) {
				if (board.getID() == boardID) {
					boards.remove(board);
					board.terminateBoard();
					break;
				}
			}
		}
	}

	/**
	 * Notifies the specified user of all previously created boards. No other
	 * boards can be added or removed until this method returns.
	 * 
	 * @param user
	 *            the User to be notified
	 */
	public void resendAllBoard(User user) {
		synchronized (boards) {
			for (MasterBoard board : boards) {
				user.notifyBoard(board);
			}
		}
	}

	/**
	 * Creates a new board with the specified name and notifies all users of
	 * this change. No other boards can be added or removed until this method
	 * returns.
	 * 
	 * @param name
	 *            the name of the board in the NAME :== [^N]+ format
	 */
	public void makeNewBoard(String name) {
		synchronized (boards) {
			boards.add(new MasterBoard(name));
		}
	}

	public void welcomeNewUsers() throws IOException {
		while (true) {
			// blocks until client attempts to connect
			final Socket socket = serverSocket.accept();
			Thread userInitThread = new Thread(new Runnable() {
				public void run() {
					try {
						handleConnection(socket);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});

			userInitThread.start();
		}
	}

	public void handleConnection(Socket socket) throws IOException {
		System.out.println("New connection from <"
				+ socket.getInetAddress().getLocalHost().getHostAddress()
				+ ">.");

		// initialize input and output streams
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

		User newUser = null;

		try {
			String user_req = in.readLine();
			if (user_req == null
					|| !user_req.matches("user_req( [A-Za-z]([A-Za-z0-9]?)+)?"))
				return;

			String username = null; // no username supplied
			if (user_req.length() > 8)
				username = user_req.substring(9); // extract username

			synchronized (users) {
				// check for duplicate username
				for (User user : users) {
					if (username == null)
						break; // cases: no username supplied or duplicate found
					if (user.getName().equals(username)) {
						username = null;
					}
				}

				// create new instance and add to list
				newUser = new User(username, socket, this);
				users.add(newUser);
			}

			// send new username to client
			out.println("you_are " + newUser.getName());
			out.flush();

			newUser.beginConnection();
			System.out.println("User \'" + newUser.getName()
					+ "\' instantiated.");

		} finally {
			out.close();
			in.close();
			socket.close();

			// may have failed on beginConnection();
			synchronized (users) {
				users.remove(newUser);
			}

			
			System.out.println("Uninstantiated user at <"
					+ socket.getInetAddress().getLocalHost().getHostAddress()
					+ "> disconnected.");
		}
	}

	public static void main(String[] args) {
		// Prompt User for Port Number
		String portInput = "55000";

		while (true) {
			// Port in range [49152,65535]
			String portPattern = "(49(1(5[2-9]|[6-9][0-9])|[2-9][0-9]{2})|5[0-9]{4}|6([0-4][0-9]{3}|5([0-4][0-9]{2}|5([0-2][0-9]|3[0-5]))))";

			JTextField addressField = new JTextField();
			JTextField portField = new JTextField();
			Object[] message = {"Port:",
					portField };

			int buttonPressed = JOptionPane.showConfirmDialog(null, message,
					"Start", JOptionPane.OK_CANCEL_OPTION);

			if (buttonPressed == JOptionPane.OK_OPTION) {
				portInput = portField.getText();
				if (portInput == null || !portInput.matches(portPattern)) {
					JOptionPane
							.showMessageDialog(
									new JFrame(),
									"An invalid port number was entered. Please re-enter a number within the range [49152,65535].",
									"Incorrect Port", JOptionPane.ERROR_MESSAGE);
					continue;
				} else{
					break; // correct port number
				}
			} else {
				System.exit(0);
			}
		}
		
		WhiteboardServer server = null;
		try {
			server = new WhiteboardServer(Integer.parseInt(portInput));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Dialog to display information about the server.
		// Closes the server when button "Kill Server" is clicked.
		JButton button = new JButton("Kill Server");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		final JOptionPane optionPane = new JOptionPane(
				"WhiteboardServer running.\nPORT: " + server.getPort()
						+ "\nADDRESS: " + server.getIP(),
				JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
				null, new Object[] { button }, null);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				final JDialog dialog = new JDialog();
				dialog.setModal(true);
				dialog.setContentPane(optionPane);
				dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				dialog.pack();
				dialog.setVisible(true);
			}
		});

		try {
			server.welcomeNewUsers();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
