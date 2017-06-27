import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Guy on 04/04/2017.
 */
public class MemoryGameClient {
    public static void main(String[]args){
        Scanner s=new Scanner(System.in);

        MemoryGamePanel panel=new MemoryGamePanel();
        JFrame window=new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(600,800);

        window.add(panel);
        window.setVisible(true);



    }

}
