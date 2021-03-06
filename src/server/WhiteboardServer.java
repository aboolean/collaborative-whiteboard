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

/*
#####################################
###### Thread Safety Arguments ######
#####################################
- Lists 'boards' and 'users' locked every time they are accessed.
Fine-grain locking occurs with individual synchronized statements.
Deadlock is avoided by locking on one list at a time or always locking
on 'users' first.
- In instances of sequential (non-nested) locking, interleaving is not
destructive due to the operation involved. (adding and removing boards)
- 'serverSocket' confined to single thread
- Individual client Socket instances confined to individual threads
until passed into User object, which is itself thread-safe in its
handling of the Socket.

######################################
######## Preserved Invariants ########
######################################
- Lists 'boards' and 'users' are declared as final; references cannot
change although contents can
- 'serverSocket' is final; server is bound to single port
- elements of 'boards' ordered by boardID
- elements of 'users' ordered by userID
- 'boards' contains active boards only
- 'users' contains connected clients only
*/

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
		users = new ArrayList<User>();
		boards = new ArrayList<MasterBoard>();

		serverSocket = new ServerSocket(listeningPort);

		System.out.println("Server running. | IP: <" + getIP() + "> | PORT: "
				+ getPort());
	}

	/**
	 * Used primarily for testing to return an integer array of board ID
	 * numbers.
	 * 
	 * @return an integer array of identification numbers
	 */
	public int[] getBoardIDNumbers() {
		int[] id;
		synchronized (boards) {
			id = new int[boards.size()];
			for (int i = 0; i < boards.size(); i++) {
				id[i] = boards.get(i).getID();
			}
		}
		return id;
	}

	/**
	 * Used primarily for testing to return a String array of names for
	 * connected users.
	 * 
	 * @return a String array of connected usernames
	 */
	public String[] getUserNames() {
		String[] names;
		synchronized (users) {
			names = new String[users.size()];
			for (int i = 0; i < boards.size(); i++) {
				names[i] = users.get(i).getName();
			}
			return names;
		}
	}

	/**
	 * Used primarily for testing to return an integer array of identification
	 * numbers for connected users.
	 * 
	 * @return an integer array of connected user ID numbers
	 */
	public int[] getUserIDNumbers() {
		int[] id;
		synchronized (users) {
			id = new int[users.size()];
			for (int i = 0; i < boards.size(); i++) {
				id[i] = users.get(i).getID();
			}
			return id;
		}
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
			return InetAddress.getLocalHost().getHostAddress();
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
		MasterBoard delBoard = null;
		synchronized (boards) {
			for (MasterBoard board : boards) {
				if (board.getID() == boardID) {
					boards.remove(board);
					delBoard = board;
					break;
				}
			}
		}

		if (delBoard != null) {
			delBoard.terminateBoard();
			synchronized (users) {
				for (User user : users) {
					user.forgetBoard(delBoard);
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
		MasterBoard newBoard = new MasterBoard(name);

		synchronized (boards) {
			boards.add(newBoard);
		}

		synchronized (users) {
			for (User user : users) {
				user.notifyBoard(newBoard);
			}
		}
	}

	/**
	 * Run after constructing this WhiteboardServer to begin accepting new users
	 * on the specified listening port.
	 * 
	 * @throws IOException
	 *             connection interrupted
	 */
	public void welcomeNewUsers() throws IOException {
		while (true) {
			// blocks until client attempts to connect
			final Socket socket = serverSocket.accept();
			Thread userInitThread = new Thread(new Runnable() {
				public void run() {
					try {
						handleConnection(socket);
					} catch (IOException e) {
						// uninstantiated user dropped; ignore
						// connections closed in handleConnection
					}
				}
			});

			userInitThread.start();
		}
	}

	/**
	 * Called from within welcomeUsers() on a background thread to set up a new
	 * user. Opens input and output streams on socket, completes username
	 * handshake with client, and constructs User instance. Closes socket and
	 * streams if connection interrupted.
	 * 
	 * @param socket
	 *            a Socket connected to a client
	 * @throws IOException
	 *             connection interrupted
	 */
	private void handleConnection(Socket socket) throws IOException {
		System.out.println("New connection from <"
				+ socket.getRemoteSocketAddress().toString() + ">.");

		// initialize input and output streams
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

		User newUser = null;

		try {
			String user_req = in.readLine();
			if (user_req == null
					|| !user_req.matches("user_req( [A-Za-z]([A-Za-z0-9]?)+)?"))
				throw new IOException(); // trip catch block

			String username = null; // no username supplied
			if (user_req.length() > 8)
				username = user_req.substring(9); // extract username

			synchronized (users) {
				// check for duplicate username
				for (User user : users) {
					if (username == null)
						break; // cases: no username supplied or duplicate found
					// check against existing username, case insensitive
					if (user.getName().toLowerCase()
							.equals(username.toLowerCase())) {
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

		} catch (IOException e) {
			// may have failed on beginConnection();
			synchronized (users) {
				users.remove(newUser);
			}

			System.out.println("Uninstantiated user at <"
					+ socket.getRemoteSocketAddress().toString()
					+ "> disconnected.");

			out.close();
			in.close();
			socket.close();
		}
	}

	/**
	 * Runs the WhiteboardServer graphically. Prompts the user for port number
	 * (defaults to 55000). Displays port and IP address of the server. Button
	 * "Kill Server" closes the server and stops listening for connections.
	 */
	public static void main(String[] args) {
		// Prompt User for Port Number
		String portInput = "55000";

		while (true) {
			// Port in range [49152,65535]
			String portPattern = "(49(1(5[2-9]|[6-9][0-9])|[2-9][0-9]{2})|5[0-9]{4}|6([0-4][0-9]{3}|5([0-4][0-9]{2}|5([0-2][0-9]|3[0-5]))))";

			JTextField portField = new JTextField();
			Object[] message = { "Port:", portField };

			int buttonPressed = JOptionPane.showConfirmDialog(null, message,
					"Start Server", JOptionPane.OK_CANCEL_OPTION);

			if (buttonPressed == JOptionPane.OK_OPTION) {
				portInput = portField.getText();
				if (portInput == null || portInput.equals("")) {
					portInput = "55000";
					break;
				} else if (!portInput.matches(portPattern)) {
					JOptionPane
							.showMessageDialog(
									new JFrame(),
									"An invalid port number was entered. Please re-enter a number within the range [49152,65535].",
									"Incorrect Port", JOptionPane.ERROR_MESSAGE);
					continue;
				} else {
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
