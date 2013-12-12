package Testing;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import client.ClientBoard;

/**
 * Testing Documentation: ClientBoardTest
 * ClientBoard is another very simple datatype. Our first test will consist of a simple valid constructor,
 * which will then test ClientBoard's get() functions for correct data storage & retrieval. Next, we will
 * test the toString() method, to ensure functionality as well as to again check for correct data storage/
 * retrieval. Finally, we test the compareTo() function, by creating three instances of ClientBoard. Two of
 * these instances will be identical, one will not. The test will compare the two identical instances with
 * the goal of returning 0, and compare two of the non-identical instances with the goal of returning false.
 * 
 */

public class ClientBoardTest
{
	@Test
	public void validConstructorTest()
	{
		// Create a valid instance of ClientBoard, then test its get() functions.
		ClientBoard testBoard = new ClientBoard("testBoard", 1);
		String expectedName = "testBoard";
		int expectedID = 1;
		assertEquals(expectedName, testBoard.getName());
		assertEquals(expectedID, testBoard.getID());
	}
	
	@Test
	public void stringTest()
	{
		// Create an instance of ClientBoard and test its toString method for correctness
		ClientBoard testBoard = new ClientBoard("ImAClientBoard", 2);
		String expected = "board 2 ImAClientBoard";
		assertEquals(expected, testBoard.toString());
	}
	
	@Test
	public void compareTest()
	{
		//Create four instances of Clientboard, with two being identical
		ClientBoard testBoard = new ClientBoard("ImAlsoAClientBoard", 5);
		ClientBoard testBoardTwin = new ClientBoard("ImAlsoAClientBoard", 5);
		ClientBoard testBoardLess = new ClientBoard("GuessWhatImAClientBoardToo", 3);
		ClientBoard testBoardGreater = new ClientBoard("HelpImStuckInAClientBoardFactory", 17);
		
		assertEquals(0, testBoard.compareTo(testBoardTwin));
		assertEquals(1, testBoard.compareTo(testBoardLess));
		assertEquals(-1, testBoard.compareTo(testBoardGreater));
	}
}
