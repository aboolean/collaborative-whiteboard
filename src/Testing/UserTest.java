package Testing;


import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.Test;

import server.WhiteboardServer;
import data.MasterBoard;
import data.User;

public class UserTest {

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
	public void userSelectBoard() throws IOException {
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
	
	@Test
	public void userHandleRequestStroke() throws IOException {
	    WhiteboardServer.main(new String[] {"arg1", "arg2", "arg3"});
	    Socket socket = new Socket(InetAddress.getByName("18.189.24.133"), 55003);
//	    User user = new User(null, socket, server);
//	    server.makeNewBoard("anotherTestBoard");
//	    user.selectBoard(1);
	    
	    //user.handleRequest("stroke 1 3 50 50 51 51 0 0 0");
	}

}
