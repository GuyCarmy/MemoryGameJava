import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Guy Carmy on 24/06/2017.
 * ID: 301726154
 *
 * this is the cards generator
 * it randomly choose the necessary amount of cards from the folder
 * 
 * cards picture source: http://vector4free.com/vector/free-vector-playing-cards-deck/
 * its free for any use, and I cropped it to a reasonable size
 */
public class CardsGenerator {
    private ArrayList<String> cards;

    //constructor
    public CardsGenerator(){
        //arraylist to hold the cards
        cards = new ArrayList<String>();
        //set the path to cards folder
        Path path = Paths.get("cards");
        //iterate through the folder and add all the cards to array list
        try {
            DirectoryStream<Path> directoryStream= Files.newDirectoryStream(path);
            for (Path p:directoryStream) {
                cards.add(p.toString());
            }
        } catch (IOException e) {
            System.out.println("failed loading cards pictures from folder");
            System.exit(1);
        }

    }



    // return the cards randomly chosen
    // maximum size is 10 by 10
    public String[] getCards(int num){
        if(num>10) {
            System.out.println("Maximum board size is 10.");
            System.exit(1);
        }
        int size=num*num;
        int numOfCards=size/2;
        //list instead of array for shuffle
        List<String> reCards = new ArrayList<String>();
        //remove random card from the list
        for (int i = 0; i < numOfCards; i++) {
            reCards.add(cards.remove((int)(Math.random()*cards.size())));
        }
        //double the array. each card as to be twice
        for (int i = 0; i < numOfCards; i++) {
            reCards.add(reCards.get(i));
        }
        //shuffle after adding the 2nd set of same cards
        Collections.shuffle(reCards);
        //make a string array for panel to use
        String[] stringCards = new String[size];
        for (int i = 0; i < size; i++) {
            stringCards[i] = reCards.get(i);
        }
        return stringCards;
    }
}
