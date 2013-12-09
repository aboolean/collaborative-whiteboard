package data;

import java.net.Socket;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import server.WhiteboardServer;

/**
 * Each instance of User represents a unique connected client. This class
 * facilitates the communication with the client over the network and process
 * incoming/outgoing messages. It also entails a server-side representation of
 * relevant client attributes.
 */
public class User {
	// basic attributes
	private final String username;
	private final int id_num;
	private static final AtomicInteger nextID = new AtomicInteger(0);

	private final WhiteboardServer server;
	private final MasterBoard board;
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
	public void incorporateBoard(MasterBoard board) {
		String info = "board " + String.valueOf(board.getID()) + " "
				+ board.getName();

		outgoingMessageQueue.put(info);
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
		String del = "del " + String.valueOf(board.getID());

		outgoingMessageQueue.put(del);
	}

	public void incorporateStroke(WhiteLine stroke) {
		
	}

	public void notifyClear() {

	}
}
