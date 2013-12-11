package Testing;


import static org.junit.Assert.*;

import java.io.IOException;
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

}
