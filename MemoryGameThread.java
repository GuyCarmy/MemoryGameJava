import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by Guy Carmy on 24/06/2017.
 * ID: 301726154
 *
 * this is the server's thread that opens for every 2 players and handles their game
 * this is most of the game server side code
 * the only show of the MemoryGame class is in here, and the clients only get the board and what to show
 *
 */
public class MemoryGameThread extends Thread {
    //connections components
    private Socket player1 = null;
    private Socket player2 = null;
    private ObjectOutputStream out1 = null;
    private ObjectOutputStream out2 = null;
    private ObjectInputStream in1 = null;
    private ObjectInputStream in2= null;
    //for turns handling (first player is the first one that connected)
    private boolean firstPlayer = true;
    //every user choose to cards, this boolean is to differ between them
    private boolean firstCard = true;
    //the MemoryGame object
    private MemoryGame game;
    //timer to count the 3 seconds before hiding if 2 cards is not match
    private Timer timer;
    //players scores
    private int player1Score;
    private int player2Score;
    //object to hold the input coming from the stream
    private Object input = null;
    //temp array for the first card the user chose
    private int[] temp;
    //board size
    private int boardSize;

    //constructor
    public MemoryGameThread(Socket player1,Socket player2) {
        //get the size as input and check if its valid (even and less then or 10)
        int size=1;
        boolean goodSize = false;
        while(!goodSize) {
            try {
                size = Integer.parseInt(JOptionPane.showInputDialog("Please enter board size \n" +
                        "It has to be an even number (integer) up to 10"));
            } catch (NumberFormatException e) {
                System.out.println("input must be a number");
            }
            if (size % 2 != 0)
                System.out.println("size has to be even");
            else if (size>10)
                System.out.println("size has to be 10 or less");
            else
                goodSize = true;
        }

        //create game object
        boardSize =size;
        game = new MemoryGame(boardSize);
        //create timer for 3 seconds and set its listener
        timer = new Timer(3000, new TimerListener());
        //set players
        this.player1 = player1;
        this.player2 = player2;
        //set streams
        try {
            out1 = new ObjectOutputStream(player1.getOutputStream());
            out2 = new ObjectOutputStream(player2.getOutputStream());
            out1.flush();
            out2.flush();
            in1 = new ObjectInputStream(player1.getInputStream());
            in2 = new ObjectInputStream(player2.getInputStream());
        } catch (IOException e) {
            System.out.println("couldn't create connections");
            return;
        }

        //reset scores
        player1Score=0;
        player2Score=0;

        //start game
        try {
            //first object sent to clients must be board size
            out1.writeObject(boardSize);
            out1.writeObject("Wait for second player");
            out2.writeObject(boardSize);
            //second object is board matrix
            out1.writeObject(game.getBoard());
            out2.writeObject(game.getBoard());
            //game started
            out1.writeObject("Game Started");
            out2.writeObject("Game Started");
            //first player connected get first turn
            out1.writeObject("Your Turn");
            out2.writeObject("Opponent's Turn");
            //clients paint panel when they get the show matrix
            out1.writeObject(game.getShow());
            out2.writeObject(game.getShow());
        } catch (IOException e) {
            System.out.println("couldn't send game to start with");
            return;
        }


    }


    @Override
    public void run() {
        //while game is not finished, server thread runs.
        while(!game.isFinished()){
            //in case first player turn
            if (firstPlayer) {
                try {
                    //gets input
                    input = in1.readObject();
                } catch (IOException e) {
                    System.out.println("couldn't read input from 1");
                    return;
                } catch (ClassNotFoundException e) {
                    System.out.println("couldn't read input from 1");
                    return;
                }
                //send to process
                processInput(input);
            //in case second player turn
            }else{
                try {
                    //gets input
                    input = in2.readObject();
                } catch (IOException e) {
                    System.out.println("couldn't read input from 2");
                    return;
                } catch (ClassNotFoundException e) {
                    System.out.println("couldn't read input from 2");
                    return;
                }
                //send to process
                processInput(input);
            }

        }
        //out of while loop means game ended
        try {
            //check who won and display to players
            if (player1Score==player2Score) {
                out1.writeObject("Its a Tie!");
                out2.writeObject("Its a Tie!");
            } else if (player1Score>player2Score){
                out1.writeObject("You Won!");
                out2.writeObject("You lost");
            } else{
                out2.writeObject("You Won!");
                out1.writeObject("You lost");
            }
            //send finished message to clients
            out1.writeObject("Finished");
            out2.writeObject("Finished");
            //close connections
            in1.close();
            in2.close();
            out1.close();
            out2.close();
            player1.close();
            player2.close();
        } catch (IOException e) {
            System.out.println("couldn't close connections to clients");
            return;
        }
    }


    //process the input from clients
    public void processInput(Object input){
        // an int array is a players play
        if (input instanceof int[]) {
            if(firstCard) { //if its first card holds the play in temp
                temp = (int[]) input;
                //show the player move to both players
                show(temp[0],temp[1]);
                //switch to second card
                firstCard=false;
            }
            else{
                //second card
                //show the move to both players
                show(((int[])input)[0],((int[])input)[1]);
                //switch to first card
                firstCard=true;
                //check if match
                if (game.tryMatch(temp,(int[])input)){
                    //its a match
                    if (firstPlayer){ //first player
                        player1Score++;
                        try {
                            out1.writeObject("Congratulations");
                        } catch (IOException e) {
                            System.out.println("couldn't send congrats");
                            return;
                        }
                    }
                    else{ //second player
                        player2Score++;
                        try {
                            out2.writeObject("Congratulations");
                        } catch (IOException e) {
                            System.out.println("couldn't send congrats");
                            return;
                        }
                    }
                }
                else {
                    //its not a match
                    //start timer to hide cards
                    timer.start();
                }

                //switch turns after player played 2 cards
                if(firstPlayer){
                    firstPlayer=false;
                    try {
                        out1.writeObject("Opponent's Turn");
                        out2.writeObject("Your Turn");
                    } catch (IOException e) {
                        System.out.println("couldn't switch turns");
                        return;
                    }
                }
                else{
                    firstPlayer=true;
                    try {
                        out2.writeObject("Opponent's Turn");
                        out1.writeObject("Your Turn");
                    } catch (IOException e) {
                        System.out.println("couldn't switch turns");
                        return;
                    }
                }
            }
        }
    }

    //timer listener is 3 seconds long and hide the cards when ends.
    public class TimerListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[][] t=new boolean[boardSize][boardSize];
            for (int i = 0; i< boardSize; i++)
                for (int j = 0; j < boardSize; j++)
                    t[i][j]=game.getShow()[i][j];
            try {
                //send new show matrix to clients
                out1.writeObject(t);
                out2.writeObject(t);
            } catch (IOException e1) {
                System.out.println("couldn't send from timer");
                return;
            }
            timer.stop();
        }
    }
    //show by indexes
    private void show(int a,int b){
        //update show matrix in the game object
        game.show(a,b);

        boolean[][] t=new boolean[boardSize][boardSize];
        for (int i = 0; i< boardSize; i++)
            for (int j = 0; j < boardSize; j++)
                t[i][j]=game.getShow()[i][j];

        //send the new show matrix to clients
        try {
            out1.writeObject(t);
            out2.writeObject(t);

        } catch (IOException e1) {
            System.out.println("couldn't send temp show");
            return;
        }
    }



}
