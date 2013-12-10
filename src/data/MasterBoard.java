package data;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Arrays;

//TODO: concurrency strategy and invariants

/**
 * MasterBoard is the central representation of a single collaborative white
 * board. It can be edited by several users.
 */
public class MasterBoard implements Comparable<MasterBoard> {
	private final String name;
	private final int id_num;
	private static final AtomicInteger nextID = new AtomicInteger(0);
	@SuppressWarnings("unused")
	private static final int Y_SIZE = 600, X_SIZE = 800;

	private final ArrayList<WhiteLine> strokes;
	private final ArrayList<User> users;

	private final PriorityBlockingQueue<WhiteLine> strokeQueue;
	private final Thread strokeThread;

	/**
	 * Constructs a MasterBoard object with the provided name. (NAME :==
	 * [^\r\n]) The ID number of the board is sequentially generated. Each
	 * instance is initialized with no users or drawn strokes.
	 * 
	 * @param name
	 *            the white board name in the NAME format
	 * @throws IllegalArgumentException
	 *             the provided name is not in the NAME format
	 */
	public MasterBoard(String name) {
		// check 'name' paramter
		if (!name.matches("[A-Za-z0-9 ]+"))
			throw new IllegalArgumentException(
					"The provided 'name' is not in the NAME :== [A-Za-z0-9 ]+ Format.");

		// assign name and ID
		id_num = nextID.getAndIncrement(); // assigns next ID number atomically
		this.name = name;

		// initialize strokes and users
		strokes = new ArrayList<WhiteLine>();
		users = new ArrayList<User>();

		// initializes queue for strokes to be made
		strokeQueue = new PriorityBlockingQueue<WhiteLine>();

		// begins processing queued strokes
		strokeThread = new Thread(new Runnable() {
			public void run() {
				processStrokes();
			}
		});
		strokeThread.start();
	}

	/**
	 * Notifies the specified user of all previously made strokes.
	 * 
	 * @param newEditor
	 *            a User to receive strokes
	 */
	private void resendAllStrokes(User newEditor) {
		// lock on strokes so not changes can be made
		synchronized (strokes) {
			for (WhiteLine line : strokes) {
				newEditor.notifyStroke(line);
			}
		}
	}

	/**
	 * Adds the provided line to the white board and sends it to all editing
	 * users. No other strokes can be added until the method returns and the
	 * users are guaranteed to receive no other strokes to preserve order.
	 * 
	 * @param line
	 *            a WhiteLine to be added to this MasterBoard
	 */
	public void makeStroke(WhiteLine line) {
		strokeQueue.put(line);
	}

	/**
	 * Called in a background thread to process all queued strokes. The
	 * BlockingQueue contains all strokes that should be reflected on the board.
	 */
	private void processStrokes() {
		while (true) {
			WhiteLine line;
			try {
				line = strokeQueue.take();
				// locking on users guarantees no other strokes are sent to them
				synchronized (users) {
					// strokes are not added in any other place
					synchronized (strokes) {
						strokes.add(line);

						/*
						 * Interleaving is not a problem here, because "strokes"
						 * cannot be concurrently accessed or modified. The
						 * users cannot call getAllStrokes() while a change is
						 * being made, so they do not "miss" any updates or
						 * receive duplicates.
						 */

						for (User user : users) {
							/*
							 * Order is preserved because each user has only one
							 * board at a time and can only belong to one
							 * MasterBoard's (locked) "users" list at a time.
							 */
							user.notifyStroke(line);
						}
					}
				}
			} catch (InterruptedException e) {
				// thread stopped; do nothing
			}
		}
	}

	/**
	 * Clears all the strokes on the board and informs all the editors of this
	 * change. The board is not allowed to be modified or accessed during this
	 * time.
	 */
	public void clearBoard() {
		// locking on users guarantees no other changes are sent to them
		synchronized (users) {
			// strokes cannot be modified or accessed during this time
			synchronized (strokes) {
				strokeQueue.clear(); // remove all strokes yet to be made
				strokes.clear(); // remove all strokes already made
			}

			// Interleaving is not a problem here, because "strokes" cannot
			// be concurrently accessed or modified. Reading all the strokes or
			// making changes must occur after the clearing has occurred and the
			// users have been notified.

			// inform users of change
			for (User user : users) {
				user.notifyClear();
			}
		}
	}

	/**
	 * Removes a user as an editor of the board. No strokes can be made or
	 * retrieved until this method returns.
	 * 
	 * @param user
	 *            a User to be added as an editor
	 */
	public void addUser(User user) {
		// lock on users guarantees no strokes made at this time
		synchronized (users) {
			// add user
			users.add(user);
			// resent all existing strokes
			this.resendAllStrokes(user);
			// send updated editors list to all connected
			String newUserList = this.getUserList();
			for (User editor : users) {
				editor.notifyEditors(newUserList);
			}
		}
	}

	/**
	 * Removes a user as an editor of the board. No strokes can be made or
	 * retrieved until this method returns.
	 * 
	 * @param user
	 *            a User to be removed as an editor
	 */
	public void removeUser(User user) {
		// lock on users guarantees no strokes made at this time
		synchronized (users) {
			// remove user
			users.remove(user);
			// send updated editors list to all connected
			String newUserList = this.getUserList();
			for (User editor : users) {
				editor.notifyEditors(newUserList);
			}
		}
	}

	/**
	 * Called when the board is being deleted. Removes all editors of the board,
	 * clears board and queued strokes. All strokes made afterward are not
	 * forwarded, since the board is in the process of deletion.
	 */
	public void terminateBoard() {
		synchronized (users) {
			users.clear();
			this.clearBoard(); // clears queued changes too
			strokeThread.interrupt();
		}
	}

	/**
	 * Returns an alphabetized, space-delimited list of current editors. No
	 * users can be added or removed at the time of accessing users, so the list
	 * is an accurate reflection of editors.
	 * 
	 * @return a list of users editing this MasterBoard
	 */
	public String getUserList() {
		User[] editors;
		// no users can be added or removed during this time
		synchronized (users) {
			editors = users.toArray(new User[users.size()]);
		}

		Arrays.sort(editors); // User.compareTo() by username

		// join string
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < editors.length; i++) {
			output.append(editors[i].toString());
			if (i != editors.length - 1)
				output.append(" ");
		}

		return output.toString();
	}

	/**
	 * Returns the assigned name of this MasterBoard.
	 * 
	 * @return the name value
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the unique, auto-generated, sequential identification number of
	 * this MasterBoard.
	 * 
	 * @returns the identification number
	 */
	public int getID() {
		return id_num;
	}

	/**
	 * Returns the properties of the board in the form of a BOARD_INFO message.
	 * For example, a MasterBoard named "Giggles" with ID number 7 would produce
	 * "board 7 Giggles".
	 * 
	 * @return a string representation of this MasterBoard
	 */
	public String toString() {
		return "board " + String.valueOf(id_num) + " " + name;
	}

	/**
	 * Compares this MasterBoard to other on the basis of identification numbers
	 * using the standard conventions of compareTo.
	 * 
	 * @return +1 if this has greater ID number than other, -1 if lesser, 0 if
	 *         equal
	 */
	public int compareTo(MasterBoard other) {
		return this.id_num > other.id_num ? 1 : this.id_num < other.id_num ? -1
				: 0;
	}

	/**
	 * Tests this MasterBoard to other MasterBoard for equality on the basis of
	 * their unique ID numbers.
	 * 
	 * @return true if other has the same ID number as this, false otherwise
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof MasterBoard))
			return false;
		return this.id_num == ((MasterBoard) other).id_num;
	}

	/**
	 * Returns a unique hash code representing this MasterBoard. The produced
	 * code is the ID of the board added to 2^25.
	 * 
	 * @return the hash code for this MasterBoard
	 */
	@Override
	public int hashCode() {
		return (int) Math.pow(2, 25) + id_num;
	}
}