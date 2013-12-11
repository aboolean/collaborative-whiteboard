package Testing;


import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import server.WhiteboardServer;
import data.MasterBoard;
import data.User;

/**
 * Testing Documentation: MasterboardTest
 * We begin by testing a basic valid implementation of Masterboard, to ensure proper construction. No errors
 * or exceptions should be thrown during its construction. Next, we test an invalid construction case, in
 * particular when the name is not within the defined regex ("[A-Za-z0-9]+"). This test should throw an error
 * as it attempts to construct the Masterboard. Next, we will test the assignment of ID numbers by creating a
 * number of valid Masterboard instances. We should be able to predict the ID numbers of these Masterboards,
 * as ID numbers are given out in progressive order, beginning at 0. Further, we test the class's toString
 * method to ensure data is stored/retreived correctly. Next, we test the ability to add/remove multiple users
 *  to a particular MasterBoard (this is basically the point of the whole project!). Finally, we test the
 *  Masterboards ability to take in various commands and apply them, either to itself (e.g. in the case of 
 *  deletion) or to clients' local copies of the board (e.g. passing along a new stroke). These tests essentially
 *  consist of applying each of Masterboard's methods and testing that the proper outcome arises.
 * 
 */

public class MasterboardTest {

	@Test
	public void simpleConstructorTest() throws IOException
	{
		// Test for simple construction of a basic MasterBoard
		MasterBoard testBoard = new MasterBoard("T3st1ngBoard");
		
		assertEquals("T3st1ngBoard", testBoard.getName());
		assertEquals(1, testBoard.getID());
		assert(testBoard.isThreadActive());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void invalidNameConstructorTest() throws IOException
	{
		// Test when provided name is not valid (e.g. not within regex "[A-Za-z0-9]+")
		MasterBoard testBoard = new MasterBoard("T3st1ngBo@rd");
	}
	
	@Test
	public void sequentialIDTest()
	{
		// Test that class successfully assigns ID numbers in progressive fashion
		MasterBoard testBoard1 = new MasterBoard("testingBoard1");
		MasterBoard testBoard2 = new MasterBoard("testingBoard2");
		MasterBoard testBoard3 = new MasterBoard("testingBoard3");
		
		assertEquals(4, testBoard1.getID());
		assertEquals(5, testBoard2.getID());
		assertEquals(6, testBoard3.getID());
	}
	
	@Test
	public void toStringTest()
	{
		//Test string representation of MasterBoard
		MasterBoard testBoard = new MasterBoard("testingBoard");
		String expected = "board 3 testingBoard";
		assertEquals(expected, testBoard.toString());
	}

	@Test
	public void addUserTest() throws IOException
	{
		// Test successfully adding new users to an active board
		MasterBoard testBoard = new MasterBoard("testingBoard");
		User user1 = new User("user1", new Socket(), new WhiteboardServer(55000));
		
		testBoard.addUser(user1);
		assert(testBoard.getUserList().contains(user1.getName()));
	}

	@Test
	public void removeUserTest() throws IOException
	{
		// Test successfully removing users from an active board
		MasterBoard testBoard = new MasterBoard("testingBoard");
		User user1 = new User("user1", new Socket(), new WhiteboardServer(55001));

		testBoard.addUser(user1);
		assert(testBoard.getUserList().contains(user1.getName()));
		testBoard.removeUser(user1);
		assert(!testBoard.getUserList().contains(user1.getName()));
	}

	@Test
	public void deleteTest()
	{
		/* Test when board is deleted. User list and queued changes should be cleared,
			and the board's strokeThread should be terminated.*/
		MasterBoard testBoard = new MasterBoard("testingBoard");
		testBoard.terminateBoard();
		
		assertEquals("", testBoard.getUserList());
		assert(!testBoard.isThreadActive());
	}
}
