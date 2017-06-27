import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Guy Carmy on 24/06/2017.
 * ID: 301726154
 *
 * this is the server class
 * it creates a server thread for every 2 clients connected.
 *
 */
public class MemoryGameServer {
    public static void main(String[]args){
        //connection components
        ServerSocket serverSocket = null;
        Socket socket1 = null;
        Socket socket2 = null;
        //listening is always true while the server is on.
        boolean listening=true;
        //creating socket
        try {
            serverSocket = new ServerSocket(7777);
        } catch (IOException e) {
            System.out.println("cant create server socket");
            System.exit(1);
        }
        System.out.println("Waiting players");
        while(listening){
            //wait for 2 players
            try {
                socket1 = serverSocket.accept();
                System.out.println("Player 1 connected");
                socket2 = serverSocket.accept();
                System.out.println("Player 2 connected");
                //create a server thread to handle the 2 players
                (new MemoryGameThread(socket1,socket2)).start();

            } catch (IOException e) {
                System.out.println("cant accept sockets");
                System.exit(1);
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            System.out.println("cant close server socket");
            System.exit(1);
        }
    }
}
