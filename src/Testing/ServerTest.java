package Testing;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import server.WhiteboardServer;

/**
 * @category no_didit
 * 
 */
public class ServerTest
{

    /**
     * This test manually sends the client's messages to the server and ensures
     * that the correct operations are completed. Note that for these operations
     * to pass, all the datatype methods which depend on server interaction must
     * function properly. This test serves as a verification of functionality
     * for both communication and datatype methods.
     * 
     * @throws IOException
     */
    @Test
    public void testHandleRequest() throws IOException {
        final WhiteboardServer server = new WhiteboardServer(50001);
        Thread runServer = new Thread(new Runnable() {
            public void run() {
                try {
                    server.welcomeNewUsers();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        runServer.start();

        // instantiate two clients
        Socket socket1 = new Socket(
                InetAddress.getLocalHost().getHostAddress(), 50001);
        Socket socket2 = new Socket(
                InetAddress.getLocalHost().getHostAddress(), 50001);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(
                socket1.getInputStream()));
        PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(
                socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);

        // test USER_REQ
        out1.println("user_req Fred");
        assertEquals("you_are Fred", in1.readLine());

        // test duplicate usernames (case-sensitive)
        out2.println("user_req FRED");
        assertEquals("you_are user1", in2.readLine());
        assertEquals(2, server.getUserNames().length);

        // test BRD_REQ
        out1.println("board_req new Board");
        String newBoard1 = in1.readLine();
        String newBoard2 = in2.readLine();
        assertEquals("board 0 new Board", newBoard1);

        // both clients receive the message
        assertEquals(newBoard2, newBoard1);

        // test BRD_ALL
        out1.println("board_all");
        String board_all = in1.readLine();
        assertEquals("board 0 new Board", board_all);

        out1.println("board_req anotherOne");
        in1.readLine();
        out1.println("board_all");
        String[] board_all_array = new String[] { "board 0 new Board",
                "board 1 anotherOne", };
        assertArrayEquals(board_all_array,
                new String[] { in1.readLine(), in1.readLine() });

        // test BRD_DEL
        out1.println("del 0");
        in1.readLine();
        out1.println("board_all");
        out2.println("board_all");

        // both clients have their board deleted
        assertEquals("board 1 anotherOne", in1.readLine());
        assertEquals("board 1 anotherOne", in2.readLine());

    }
}