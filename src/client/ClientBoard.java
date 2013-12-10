package client;

public class ClientBoard {

    final String name;
    final int id_num;

    /**
     * Constructs a ClientBoard object with the provided name and ID.
     * 
     * @param name
     *            the whiteboard name
     * @param id_num
     *            the whiteboard ID
     */
    public ClientBoard(String name, int id_num) {
        this.name = name;
        this.id_num = id_num;
    }

    /**
     * Returns the assigned name of this ClientBoard.
     * 
     * @return the name value
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ID of this ClientBoard.
     * 
     * @return the ID number
     */
    public int getID() {
        return id_num;
    }

    /**
     * Compares two ID numbers of two ClientBoard objects.
     * 
     * @param other
     *            the ClientBoard to be compared with
     * @return Returns a negative integer if the ID of this ClientBoard is less
     *         than the ID of the other ClientBoard, a positive integer if the
     *         ID of this ClientBoard is greater than the ID of the other
     *         ClientBoard, or 0 if the ID numbers of both ClientBoard objects
     *         are equal.
     * @throws ClassCastException
     */
    public int compareTo(Object other) throws ClassCastException {
        if (!(other instanceof ClientBoard)) {
            throw new ClassCastException();
        } else {
            ClientBoard cb = (ClientBoard) other;
            return this.getID() - cb.getID();
        }

    }

    /**
     * Returns the properties of the board in the board of a BOARD_INFO message.
     * For example, a MasterBoard named "Giggles" with ID number 7 would produce
     * "board 7 Giggles".
     * @return a String representation of this ClientBoard.
     */
    public String toString() {
        return "board " + String.valueOf(getID()) + " "
                + String.valueOf(getName());
    }

}
