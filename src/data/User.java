package data;

import java.awt.BasicStroke;
import java.awt.Color;
import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.*;

import server.WhiteboardServer;

/**
 * Each instance of User represents a unique connected client. This class
 * facilitates the communication with the client over the network and process
 * incoming/outgoing messages. It also entails a server-side representation of
 * relevant client attributes.
 */
public class User implements Comparable<User> {
	// basic attributes
	private final String username;
	private final int id_num;
	private static final AtomicInteger nextID = new AtomicInteger(0);

	private final WhiteboardServer server;
	private MasterBoard board;
	private final Socket socket;

	/*
	 * There are two queues maintained for
	 */
	private final PriorityBlockingQueue<String> outgoingMessageQueue;
	private final PriorityBlockingQueue<String> outgoingStrokeQueue;

	/**
	 * Constructs a new User corresponding to a single connected client.
	 * 
	 * @param username
	 *            the assigned user name of the client in the USER_NAME :==
	 *            [A-Za-z0-9]+ format
	 * @param socket
	 *            the Socket connected to the client
	 * @param server
	 *            the main WhiteboardServer instance
	 */
	public User(String username, Socket socket, WhiteboardServer server) {
		// assign unique sequential ID number
		id_num = nextID.getAndIncrement();

		// basic properties
		this.username = username;
		this.socket = socket;
		this.server = server;

		// no white board selected
		board = null;

		/*
		 * The User maintains two queues of messages that need to be sent to the
		 * client over the network. The 'outgoingStrokeQueue' is dedicated to
		 * sending STROKE messages to the client. To accommodate for fast board
		 * switching, this board is segregated so that it can be cleared
		 * readily. The 'outgoingMessageQueue' is meant to contain all other
		 * messages.
		 */
		outgoingMessageQueue = new PriorityBlockingQueue<String>();
		outgoingStrokeQueue = new PriorityBlockingQueue<String>();
	}

	/**
	 * Queues a BRD_INFO message for the specified board to be sent to the
	 * client with priority. This is called when a new board has been created on
	 * the server, which must notify the clients of this change.
	 * 
	 * @param board
	 *            the new MasterBoard available for use
	 */
	public void notifyBoard(MasterBoard board) {
		String info_msg = "board " + String.valueOf(board.getID()) + " "
				+ board.getName();

		outgoingMessageQueue.put(info_msg);
	}

	/**
	 * Queues a BRD_DEL message for the specified board to be sent to the client
	 * with priority. This is called by the server to notify clients that a
	 * board has been deleted centrally.
	 * 
	 * @param board
	 *            the deleted MasterBoard no longer available for use
	 */
	public void forgetBoard(MasterBoard board) {
		String del_msg = "del " + String.valueOf(board.getID());

		outgoingMessageQueue.put(del_msg);
	}

	/**
	 * Queues a STROKE message for the specified stroke to be sent to the client
	 * without priority. This is called by the server on all editors of a board
	 * to notify them that a new line has been drawn on the board.
	 * 
	 * @param stroke
	 *            a WhiteLine drawn on the current board
	 */
	public void notifyStroke(WhiteLine stroke) {
		/*
		 * Note that this method is only called for a WhiteLine that is on the
		 * current board. When selecting a board, this User dissociates itself
		 * from the previous board -- thus not receiving any updates from it --
		 * before adding itself to the new board. Since MasterBoard is
		 * independently thread-safe, we know that a user change cannot occur
		 * while strokes are being drawn.
		 */

		// no board selected (should never happen when method called)
		if (board == null) {
			return;
		}

		// generate STROKE message
		String thickness = String.valueOf(Math.round(stroke.getThickness()
				.getLineWidth()));
		String coords = String.valueOf(stroke.getX1()) + " "
				+ String.valueOf(stroke.getY1()) + " "
				+ String.valueOf(stroke.getX2()) + " "
				+ String.valueOf(stroke.getY2());
		String color = String.valueOf(stroke.getColor().getRed()) + " "
				+ String.valueOf(stroke.getColor().getGreen()) + " "
				+ String.valueOf(stroke.getColor().getBlue());
		String stroke_msg = "stroke " + String.valueOf(board.getID()) + " "
				+ thickness + " " + coords + " " + color;

		outgoingMessageQueue.put(stroke_msg);
	}

	/**
	 * Queues a BRD_CLR message to be sent to the client with priority. This is
	 * called by the server on all editors of a board to notify them that the
	 * board has been cleared.
	 */
	public void notifyClear() {
		outgoingMessageQueue.put("board_clear");
	}

	/**
	 * Queues a BRD_USERS message to be sent to the client with priority. This
	 * is called by the server when the list of editors of the current board has
	 * changed.
	 * 
	 * @param editorList
	 *            an alphabetized, space-delimited list of current editors
	 */
	public void notifyEditors(String editorList) {
		outgoingMessageQueue.put("board_users " + editorList.trim());
	}

	/**
	 * Selects the specified board as the one currently being edited. This User
	 * removes itself as an editor of the current board before clearing all
	 * queued outgoing STROKE message. The desired board is then requested from
	 * the server. If it exists, this User adds itself as an editor and saves it
	 * as it's current board; otherwise, no board is selected.
	 * 
	 * @param boardID
	 *            the identification number of the desired board
	 */
	public void selectBoard(int boardID) {
		// remove from previous board
		board.removeUser(this); // notifyStroke no longer called from prev board
		// clear STROKE notification queue
		outgoingStrokeQueue.clear();
		// use new board
		board = server.fetchBoard(boardID);
		// if new board exists, add self
		// if deleted, user should soon receive deletion message
		if (board != null) {
			board.addUser(this); // all old strokes resent here by MasterBoard
		}
	}

	/**
	 * Called from a background thread to handle messages received from client.
	 * Messages are processed one at a time. Each message should conform to the
	 * CtoS_MSG format, omitting the end line character, as this is take care of
	 * elsewhere. (CtoS_MSG :== (STROKE | SEL | BRD_REQ | BRD_DEL | BRD_ALL) N)
	 * 
	 * @param msg
	 *            a message received over network
	 * @throws RuntimeException
	 *             unrecognized command received
	 */
	private void handleRequest(String msg) {
		String[] t = msg.split("\\s"); // tokens

		// STROKE
		if (msg.matches("stroke \\d+ ([1-9]|10) \\d+ \\d+ \\d+ \\d+ \\d{1,3} \\d{1,3} \\d{1,3}")) {
			int r = Integer.parseInt(t[7]), g = Integer.parseInt(t[8]), b = Integer
					.parseInt(t[9]); // RGB values
			Color color = new Color(r, g, b);
			int thickness = Integer.parseInt(t[2]);
			int x1 = Integer.parseInt(t[3]), y1 = Integer.parseInt(t[4]);
			int x2 = Integer.parseInt(t[5]), y2 = Integer.parseInt(t[6]);
			board.makeStroke(new WhiteLine(x1, y1, x2, y2, color, thickness));
		}
		// SEL
		else if (msg.matches("select \\d+")) {
			this.selectBoard(Integer.parseInt(t[1]));
		}
		// BRD_DEL
		else if (msg.matches("del \\d+")) {
			server.deleteBoard(Integer.parseInt(t[1]));
		}
		// BRD_ALL
		else if (msg.matches("board_all")) {
			server.resendAllBoard(this);
		}
		// BRD_REQ
		else if (msg.matches("board_req .+")) {
			String name = msg.substring(10); // everything after "board_req\\s"
			server.makeNewBoard(name);
		} else {
			throw new RuntimeException(
					"Unrecognized command received from client.");
		}
	}

	/**
	 * Returns the user name of this User.
	 * 
	 * @return the name value
	 */
	public String getName() {
		return username;
	}

	/**
	 * Returns the unique, auto-generated, sequential identification number of
	 * this User.
	 * 
	 * @returns the identification number
	 */
	public int getID() {
		return id_num;
	}

	/**
	 * Compares users for ordering on the basis of user names, using standard
	 * String conventions of lexicographic comparison.
	 * 
	 * @param other
	 *            a User instance
	 * @return +1 if this is lexicographically greater than other, -1 if lesser,
	 *         0 if equal
	 */
	@Override
	public int compareTo(User other) {
		return this.username.compareTo(other.username);
	}

	/**
	 * Compare this User to other for equality on the basis of identification
	 * numbers. If the provided Object is not of type User, returns false.
	 * 
	 * @param other
	 *            an Object instance
	 * @return true if Users have same ID number, false otherwise
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof User))
			return false;
		return this.id_num == ((User) other).id_num;
	}

	/**
	 * Returns a unique hash code representing this User. The produced code is
	 * the ID of the user added to 2^26.
	 * 
	 * @return the hash code for this MasterBoard
	 */
	@Override
	public int hashCode() {
		return (int) Math.pow(2, 26) + id_num;
	}

	/**
	 * Returns the ID number and user name of this User in string format. For
	 * instance, a user named "juicymarker10" with ID number 13 would produce
	 * "user 13 juicymarker10".
	 * 
	 * @return a string representation of this User
	 */
	@Override
	public String toString() {
		return "user " + String.valueOf(id_num) + " " + username;
	}
}
