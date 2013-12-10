package server;

import data.*;

public class WhiteboardServer {

	/**
	 * Returns the board corresponding to the provided boardID. If this board
	 * does not exist, returns null.
	 * 
	 * @param boardID
	 *            the identification number of a board
	 * @return the MasterBoard or null
	 */
	public MasterBoard fetchBoard(int boardID) {
		return null;
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

	}

	/**
	 * Notifies the specified user of all previously created boards. No other
	 * boards can be added or removed until this method returns.
	 * 
	 * @param user
	 *            the User to be notified
	 */
	public void resendAllBoard(User user) {

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

	}
}
