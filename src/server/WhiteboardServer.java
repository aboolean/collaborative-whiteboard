package server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
	 * @throws IOException
	 */
	public WhiteboardServer() throws IOException {
		// initialize users and boards
		// users = Collections.synchronizedList(new ArrayList<User>());
		users = new ArrayList<User>();
		boards = new ArrayList<MasterBoard>();

		serverSocket = new ServerSocket(55000);
		String hostAddress = serverSocket.getInetAddress().getLocalHost().getHostAddress();

        // Dialog to display information about the server. Closes the server
        // when button is clicked.
        JButton button = new JButton("Kill Server");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                System.exit(0);
            }
        });
        final JOptionPane optionPane = new JOptionPane(
                "WhiteboardServer running.\nPORT: "
                        + serverSocket.getLocalPort() + "\nADDRESS: "
                        + hostAddress, JOptionPane.INFORMATION_MESSAGE,
                JOptionPane.DEFAULT_OPTION, null, new Object[] { button }, null);
        final JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setVisible(true);
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
		// TODO IP address
		System.out.println("New user connected from ");

		// initialize input and output streams
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

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
				User newUser = new User(username, socket, this);
				users.add(newUser);
			}

		} finally {
			out.close();
			in.close();
			socket.close();
		}
	}

	public static void main(String[] args) {
		try {
			WhiteboardServer server = new WhiteboardServer();
			server.welcomeNewUsers();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
