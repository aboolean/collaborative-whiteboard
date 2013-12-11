package Testing;


import static org.junit.Assert.*;

import java.io.IOException;
import java.net.Socket;

import org.junit.Test;

import server.WhiteboardServer;
import data.MasterBoard;
import data.User;

public class UserTest
{
	@Test
	public void newUserTest() throws IOException
	{
		// Test creating a new user with an input username, and one without a given name
		User user1 = new User("testUser", new Socket(), new WhiteboardServer(55000));
		User user2 = new User(null, new Socket(), new WhiteboardServer(55001));
		
		assertEquals("testUser", user1.getName());
		assertEquals(0, user1.getID());
		assertEquals("user1", user2.getName());
		assertEquals(1, user2.getID());
	}
	
	@Test
	public void noNameOverlapTest() throws IOException
	{
		// Create a new user with the default naming convention
		// Attempt to create a second user with the same name
		// Repeated username should be rejected and replaced with default
		WhiteboardServer testServer = new WhiteboardServer(56000);
		User user1 = new User("testUser1", new Socket(), testServer);
		User user2 = new User(null, new Socket(), testServer);
		
		
	}
	
	public void userSelectBoard() throws IOException
	{
	    WhiteboardServer server = new WhiteboardServer(55002);
	    server.makeNewBoard("testBoard");
	    MasterBoard mb = server.fetchBoard(0);
	    User user = new User("testUser", new Socket(), server);
	    int noBoard = user.currentBoardID();
	    user.selectBoard(0);
	    int currentBoard = user.currentBoardID();
	    assertEquals(noBoard, -1);
	    assertEquals(mb.getUserList(), "user 2 testUser");
	    assertEquals(currentBoard, mb.getID());
	}

}
