package client;

/*
#####################################
###### Thread Safety Arguments ######
#####################################
- accessed from single background thread in GUI

######################################
######## Preserved Invariants ########
######################################
- immutable data class
- name and id_num are final
*/

/**
 * ClientBoard is the client-side representation of a whiteboard. It couples a
 * board's name and ID. The name and ID of the ClientBoard are identical to
 * those of the corresponding server-side MasterBoard.
 * 
 */
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
     * Compares this ClientBoard to other on the basis of identification numbers
     * using the standard conventions of compareTo.
     * 
     * @return +1 if this has greater ID number than other, -1 if lesser, 0 if
     *         equal
     */
    public int compareTo(ClientBoard other) {
        return this.id_num > other.id_num ? 1 : this.id_num < other.id_num ? -1
                : 0;
    }

    /**
     * Returns the properties of the board in the board of a BOARD_INFO message.
     * For example, a MasterBoard named "Giggles" with ID number 7 would produce
     * "board 7 Giggles".
     * 
     * @return a String representation of this ClientBoard.
     */
    public String toString() {
        return "board " + String.valueOf(getID()) + " "
                + String.valueOf(getName());
    }

}
