package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import data.*;

public class WhiteboardServer {
	private final List<User> users;
	private final List<MasterBoard> boards;
	
	private final ServerSocket serverSocket;

	/*
	 * Invariants: - boards is always sorted by ID number - users is always
	 * sorted by ID number
	 */
	
	public WhiteboardServer(int listeningPort) throws IOException {
		// initialize users and boards
		// users = Collections.synchronizedList(new ArrayList<User>());
		users = new ArrayList<User>();
		boards = new ArrayList<MasterBoard>();
		
		serverSocket = new ServerSocket(listeningPort);
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
				if(board.getID() == boardID){
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
				if(board.getID() == boardID){
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
			for(MasterBoard board: boards){
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
		synchronized (boards){
			boards.add(new MasterBoard(name));
		}
	}
}
