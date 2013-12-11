package Testing;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.runners.MethodSorters;
import org.junit.*;

import server.WhiteboardServer;
import data.MasterBoard;
import data.User;


public class UserTest
{
    @Test
    public void newUserTest() throws IOException {
        // Test creating a new user with an input username, and one without a
        // given name
        User user1 = new User("testUser", new Socket(), new WhiteboardServer(
                55000));
        User user2 = new User(null, new Socket(), new WhiteboardServer(50000));

        assertEquals("testUser", user1.getName());
        assertEquals(0, user1.getID());
        assertEquals("user1", user2.getName());
        assertEquals(1, user2.getID());
    }
	
    /**
     * Create a new user with the default naming convention. Attempt to create a
     * second user with the same name. Repeated username should be rejected and
     * replaced with default. Note that usernames are not case sensitive.
     * @throws IOException 
     * 
     */
    @Test
    public void testNoNameOverlapTest() throws IOException {
        Socket socket = new Socket();
        WhiteboardServer server = new WhiteboardServer(50004);
        User u1 = new User("user", socket, server);
        User u2 = new User("USER", socket, server);
        System.out.println(u2.getName());
        assertEquals("user", u1.getName());
        
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
	
    /**
     * Tests the client's selection of a new board. This tests that the new User
     * begins with no board selected, that the newly selected board is informed
     * of its new editor upon selection, and that the user successfully obtains
     * the new board.
     * 
     * @throws IOException
     */
    @Test
    public void userSelectBoard() throws IOException {
        WhiteboardServer server = new WhiteboardServer(50002);
        server.makeNewBoard("testBoard");
        MasterBoard mb = server.fetchBoard(0);
        User user = new User("testUser", new Socket(), server);
        int noBoard = user.currentBoardID();
        user.selectBoard(0);
        int currentBoardID = user.currentBoardID();
        assertEquals(noBoard, -1);
        assertEquals(mb.getUserList(), "testUser");
        assertEquals(currentBoardID, mb.getID());
    }
}