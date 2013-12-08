package data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.Arrays;

//TODO: concurrency strategy and invariants

/**
 * MasterBoard is the central representation of a single collaborative white
 * board. It can be edited by several users.
 */
public class MasterBoard {
	private final String name;
	private final int id_num;
	private static final AtomicInteger nextID = new AtomicInteger(0);
	@SuppressWarnings("unused")
	private static final int Y_SIZE = 600, X_SIZE = 800;

	private final ArrayList<WhiteLine> strokes;
	private final ArrayList<User> users;

	/**
	 * Constructs a MasterBoard object with the provided name. (NAME :==
	 * [A-Za-z0-9 ]+) The ID number of the board is sequentially generated. Each
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
	}

	/**
	 * Returns all the strokes made on the board thus far. No strokes are
	 * allowed to be made until the method returns.
	 * 
	 * @return the strokes on the board as a WhiteLine array
	 */
	public WhiteLine[] getAllStrokes() {
		// lock on strokes so not changes can be made
		synchronized (strokes) {
			return strokes.toArray(new WhiteLine[strokes.size()]);
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
		// locking on users guarantees no strokes are sent to them
		synchronized (users) {
			// strokes are not added in any other place; must lock on users
			strokes.add(line);

			for (User user : users) {
				// no interleaving problems (for stroke order) because each user
				// has only one board at a time
				user.incorporateStroke(line);
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
			users.add(user);
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
			users.remove(user);
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
	 * Returns a unique hash code representing the MasterBoard. The produced code
	 * is the ID of the board added to 2^25.
	 * @return the hash code for this MasterBoard
	 */
	@Override
	public int hashCode(){
		return (int)Math.pow(2, 25) + id_num;
	}
}
