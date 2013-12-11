package Testing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import server.WhiteboardServer;
import data.MasterBoard;
import data.User;

/**
 * Testing Documentation: MasterboardTest We begin by testing a basic valid
 * implementation of Masterboard, to ensure proper construction. No errors or
 * exceptions should be thrown during its construction. Next, test the
 * construction of a MasterBoard with an invalid name (i.e., does not follow the
 * (NAME :== [^\r\n] format). Next, we will test the assignment of ID numbers by
 * creating a number of valid Masterboard instances. We should be able to
 * predict the ID numbers of these Masterboards, as ID numbers are given out in
 * progressive order, beginning at 0. Further, we test the class's toString
 * method to ensure data is stored/retreived correctly. To verify behavior that
 * is dependent upon server interaction, please see ServerTest.java
 * 
 */

public class MasterboardTest {

    /*
     *  Test for simple construction of a basic MasterBoard
     */
    @Test
    public void simpleConstructorTest() throws IOException
    {
        MasterBoard testBoard = new MasterBoard("T3st1ngBoard");
        
        assertEquals("T3st1ngBoard", testBoard.getName());
        assertEquals(0, testBoard.getID());
        assert(testBoard.isThreadActive());
    }
    
    /*
     * Test that an invalid name throws an exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalidNameConstructorTest() throws IOException
    {

        MasterBoard testBoard = new MasterBoard("oh look \n new line");
    }
    
    @Test
    public void sequentialIDTest()
    {
        // Test that class successfully assigns ID numbers in progressive fashion
        MasterBoard testBoard1 = new MasterBoard("testingBoard1");
        MasterBoard testBoard2 = new MasterBoard("testingBoard2");
        MasterBoard testBoard3 = new MasterBoard("testingBoard3");
        
        assertEquals(testBoard1.getID()+1, testBoard2.getID());
        assertEquals(testBoard2.getID()+1, testBoard3.getID());
    }
    
    @Test
    public void toStringTest()
    {
        //Test string representation of MasterBoard
        MasterBoard testBoard = new MasterBoard("testingBoard");
        String expected = "board " + testBoard.getID() + " testingBoard";
        assertEquals(expected, testBoard.toString());
    }

    /* Test when board is deleted. User list and queued changes should be cleared,
            and the board's strokeThread should be terminated.*/
    @Test
    public void deleteTest()
    {
        MasterBoard testBoard = new MasterBoard("testingBoard");
        testBoard.terminateBoard();
        
        assertEquals("", testBoard.getUserList());
        assert(!testBoard.isThreadActive());
    }
}
