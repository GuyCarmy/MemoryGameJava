import javax.swing.*;
import java.awt.*;
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
 * this is the panel for the client side.
 * it is also runnable so the GUI will work simultaneously
 *
 * client gets the board size on the start
 * it gets the board matrix once
 * then each turn it sends the play (indexes user clicked) and get a new matrix show to repaint panel
 * in addition client gets messages and display them to user (and make actions according to message)
 *
 * cards picture source: http://vector4free.com/vector/free-vector-playing-cards-deck/
 * its free for any use, and I cropped it to a reasonable size
 */
public class MemoryGamePanel extends JPanel implements Runnable{
    //the cards pictures size
    private final int cardHeight=92, cardWidth=66;
    //the board size
    private int boardSize;
    //panel components
    private JPanel cardsPanel = null;
    private JTextArea textArea=null;
    //streams
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    //boolean for turns
    private boolean myTurn = false;
    //for the back side of the cards
    private Image blankImg = null;
    //the show and board metrices
    private boolean[][] show;
    private String[][] board;
    //boolean to set true when the game ends
    private boolean gameOver=false;

    //constructor
    public MemoryGamePanel(){
        //get host name and port or set as default (localhost) if no input
        String serverName;
        int port;
        serverName = JOptionPane.showInputDialog("Please enter Host Name");
        if (serverName==null||serverName.isEmpty())
            serverName  = "localhost"; //default
        try {
            port = Integer.parseInt(JOptionPane.showInputDialog("Please enter port"));
        }catch (NumberFormatException e){
            port = 7777; //default
        }
        //create a socket and set in and out streams
        Socket socket=null;
        try{
            socket = new Socket(serverName,port);
            out=new ObjectOutputStream(socket.getOutputStream());
            in=new ObjectInputStream(socket.getInputStream());
        }catch (IOException e){
            System.out.println("couldn't connect");
            System.exit(1);
        }

        //get first input (must be the board size)
        Object input = null;
        try {
            input = in.readObject();
        } catch (IOException e) {
            System.out.println("couldn't get game");
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.out.println("couldn't get game");
            System.exit(1);
        }
        boardSize =(int)input;
        //set the matrices
        show=new boolean[boardSize][boardSize];
        board=new String[boardSize][boardSize];
        //set the back side of cards
        blankImg= this.getToolkit().getImage("blank/blank.gif");
        //set the layout of the panel
        this.setLayout(new BorderLayout());
        cardsPanel = new JPanel(new GridLayout(boardSize, boardSize));
        this.add(cardsPanel,BorderLayout.CENTER);
        textArea= new JTextArea(8,30);
        textArea.setEditable(false);
        JScrollPane jsp=new JScrollPane(textArea);
        this.add(jsp,BorderLayout.SOUTH);
        //run this thread
        new Thread(null,this).start();
    }


    @Override
    public void run() {
        Object input = null;
        //while not game over get input through stream
        while(!gameOver){
            try {
                input = in.readObject();
            } catch (IOException e) {
                System.out.println("couldn't get input");
                System.exit(1);
            } catch (ClassNotFoundException e) {
                System.out.println("couldn't get input");
                System.exit(1);
            }
            //send input to process method
            processInput(input);
        }

    }

    //process the input object
    public void processInput(Object input) {
        //case input is string matrix its the board itself with card names.
        if (input instanceof String[][]){
            board=(String[][])input;
        }
        //case input is a string the process the message
        else if (input instanceof String) {
            String msg = null;
            msg = (String) input;
            switch (msg) {
                case "Finished": //game over
                    gameOver=true;
                    break;
                case "Wait for second player": //shown only to first player until second player connects
                    textArea.setText(msg);
                    break;
                case "Game Started": //game started
                    textArea.setText(msg+"\n");
                    break;
                case "Your Turn": //let the user know its his turn
                    myTurn = true; //set the boolean as true
                    textArea.append(msg+"\n");
                    break;
                case "Opponent's Turn": //let the user know its opponents turn
                    myTurn = false; //set boolean false (set buttons listener off)
                    textArea.append(msg+"\n");
                    break;
                default:
                    //other messaged that doesn't require any additional actions
                    textArea.append(msg+"\n");
            }
        }
        //case input is a boolean matrix then its the show matrix
        else if(input instanceof boolean[][]){
            show=null;
            show=(boolean[][])input;
            //the show matrix refreshed so panel as to be repainted
            paintPanel();
        }
    }

    //paints the panel
    public void paintPanel(){
        //in case of repaint, first clear
        cardsPanel.removeAll();
        Image cardImg = null;
        //width and height for cards pictures
        int w,h;
        //default is cards size
        w=cardWidth;
        h=cardHeight;
        // set the pictures to the buttons and the buttons action command
        //(make a ImageIcon and if button is smaller then picture, resize picture)
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if(show[i][j]) {
                    cardImg = this.getToolkit().getImage(board[i][j]);
                    if((cardsPanel.getWidth()/ boardSize)<cardWidth || (cardsPanel.getHeight()/ boardSize)<cardHeight){
                        w=cardsPanel.getWidth()/ boardSize;
                        h=cardsPanel.getHeight()/ boardSize;
                    }
                    Image resizedCard=cardImg.getScaledInstance(w,h,Image.SCALE_DEFAULT);
                    ImageIcon icon=new ImageIcon(resizedCard);
                    JButton button = new JButton(icon);
                    String buttonActionCommand=i+","+j;
                    button.setActionCommand(buttonActionCommand);
                    cardsPanel.add(button);
                }
                else{
                    if((cardsPanel.getWidth()/ boardSize)<cardWidth || (cardsPanel.getHeight()/ boardSize)<cardHeight){
                        w=cardsPanel.getWidth()/ boardSize;
                        h=cardsPanel.getHeight()/ boardSize;
                    }
                    Image resizedCard=blankImg.getScaledInstance(w,h,Image.SCALE_DEFAULT);
                    ImageIcon icon=new ImageIcon(resizedCard);
                    JButton button = new JButton(icon);
                    String buttonActionCommand=i+","+j;
                    button.setActionCommand(buttonActionCommand);
                    button.addActionListener(new BtnListener());
                    cardsPanel.add(button);
                }

            }
        }
        //revalidate the panel
        cardsPanel.revalidate();
    }

    //paint component is untouched, no drawings, only buttons
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    //buttons click listener
    public class BtnListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            //if my turn is false user cant click
            if (myTurn) {
                String[] action = e.getActionCommand().split(",");
                int i = Integer.parseInt(action[0]);
                int j = Integer.parseInt(action[1]);
                int[] myPlay=new int[2];
                myPlay[0]=i;
                myPlay[1]=j;
                //send user play to server to deal with
                try {
                    out.writeObject(myPlay);
                } catch (IOException e1) {
                    System.out.println("couldnt send my play");
                    System.exit(1);
                }


            }
        }
    }

}
