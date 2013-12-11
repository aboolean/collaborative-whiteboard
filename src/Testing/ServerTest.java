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
public class ServerTest {

    /**
     * This test manually sends the client's messages to the server and ensures
     * that the correct operations are completed.
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
        Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(),
                50001);
        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // test USER_REQ
        out.println("user_req Fred");
        String you_are_fred = in.readLine();
        assertEquals("you_are Fred", you_are_fred);
        
        // test duplicate usernames (case-sensitive)
        out.println("user_req Fred");
        System.out.println("# Users: " + server.getUserNames().length);

        // test BRD_REQ
        out.println("board_req new Board");
        String newBoard = in.readLine();
        assertEquals("board 1 new Board", newBoard);

        // test BRD_ALL
        out.println("board_all");
        String board_all = in.readLine();
        assertEquals("board 1 new Board", board_all);

        out.println("board_req anotherOne");
        in.readLine();
        out.println("board_all");
        String[] board_all_array = new String[] { "board 1 new Board",
                "board 2 anotherOne", };
        assertArrayEquals(board_all_array,
                new String[] { in.readLine(), in.readLine() });

        // test BRD_DEL
        out.println("del 1");
        out.println("board_all");
        assertEquals("board 2 anotherOne", in.readLine());

    }

}
