import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Poker {
    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public int getValue() {
            if (value == "A") return 1;
            else if (value == "J") return 11;
            else if (value == "Q") return 12;
            else if (value == "K") return 13;
            return Integer.parseInt(value); //2-10
        }

        public int getSuit(){
            if (type == "C") return 1;
            else if (type == "D") return 2;
            else if (type == "H") return 3;
            return 4;
        }

        public String stringValue(){
            if (value == "A") return "Ace";
            else if (value == "J") return "Jack";
            else if (value == "Q") return "Queen";
            else if (value == "K") return "King";
            return value; //2-10
        }

        public String stringType(){
            if (type == "C") return "Clubs";
            else if (type == "D") return "Diamonds";
            else if (type == "H") return "Hearts";
            return "Spades";
        }

        public String toString() {
            return value + "-" + type;
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck;
    Random random = new Random(); //shuffle deck

    // Community Cards
    ArrayList<Card> community;

    // Player Hand
    ArrayList<Card> playerHand;

    // Pooled Cards
    ArrayList<Card> pool;

    // Progression
    int progress;

    // Window Size
    int boardWidth = 610;
    int boardHeight = boardWidth;

    int cardWidth = 110; //ratio should 1/1.4
    int cardHeight = 154;

    JFrame frame = new JFrame("Poker");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            try {
                // Draw Community Cards
                for (int i = 0; i < community.size(); i++) {
                    Card card = community.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, 20 + (cardWidth + 5)*i, 20, cardWidth, cardHeight, null);
                }

                // Draw Player Hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImg = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImg, 185 + (cardWidth + 5)*i, 320, cardWidth, cardHeight, null);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton nextButton = new JButton("Next");
    JButton restart = new JButton("Restart");
    JButton printBeat = new JButton("Print Beaters");
    JLabel handLabel = new JLabel("None");

    Poker() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        nextButton.setFocusable(false);
        buttonPanel.add(nextButton);
        restart.setFocusable(false);
        buttonPanel.add(restart);
        printBeat.setFocusable(false);
        buttonPanel.add(printBeat);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        handLabel.setFont(new Font("Arial", Font.BOLD, 14));
        handLabel.setForeground(Color.WHITE);
        handLabel.setHorizontalAlignment(JLabel.CENTER);

        gamePanel.add(handLabel, BorderLayout.CENTER);

        
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progress++;
                if(progress == 1){
                    Card card1 = deck.remove(deck.size()-1);
                    Card card2 = deck.remove(deck.size()-1);
                    Card card3 = deck.remove(deck.size()-1);
                    community.add(card1);
                    pool.add(card1);
                    community.add(card2);
                    pool.add(card2);
                    community.add(card3);
                    pool.add(card3);
                    
                    int num = getBestHand(pool);
                    handLabel.setText(handText(num) + ": You Beat " + calculateLosingHands(num, community, deck) + "/" + deck.size()*(deck.size() - 1)/2 + " hands.");
                } else {
                    Card card = deck.remove(deck.size()-1);
                    community.add(card);
                    pool.add(card);

                    int num = getBestHand(pool);
                    handLabel.setText(handText(num) + ": You Beat " + calculateLosingHands(num, community, deck) + "/" + deck.size()*(deck.size() - 1)/2 + " hands.");
                }
                if (progress >= 3) { // reached the river card
                    nextButton.setEnabled(false);

                }
                gamePanel.repaint();
            }
        });

        restart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startGame();
                nextButton.setEnabled(true);
                gamePanel.repaint();
            }
        });

        printBeat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int num = getBestHand(pool);
                printBeaters(num, community, deck);
            }
        });

        gamePanel.repaint();
    }

    public void startGame() {
        // Deck
        buildDeck();
        shuffleDeck();

        // Community Cards
        community = new ArrayList<Card>();

        // Pooled Cards
        pool = new ArrayList<Card>();

        progress = 0;

        // PLayer Hand
        playerHand = new ArrayList<Card>();

        for (int i = 0; i < 2; i++) {
            Card card = deck.remove(deck.size()-1);
            playerHand.add(card);
            pool.add(card);
            int num = getBestHand(pool);
            handLabel.setText(handText(num) + ": You Beat " + calculateLosingHands(num, community, deck) + "/" + deck.size()*(deck.size() - 1)/2 + " hands.");
        }
    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (int i = 0; i < types.length; i++) {
            for (int j = 0; j < values.length; j++) {
                Card card = new Card(values[j], types[i]);
                deck.add(card);
            }
        }

        System.out.println("BUILD DECK:");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currCard);
        }

        System.out.println("AFTER SHUFFLE");
        System.out.println(deck);
    }

    public int getBestHand(ArrayList<Card> pool){
        int best = 0;
        int[] values = new int[13];
        int[] suits = new int[4];
        for(int i = 0; i < pool.size(); i++){
            values[(pool.get(i)).getValue()-1]++;
            suits[(pool.get(i)).getSuit()-1]++;
        }
        int pairCount = 0;
        int test = 0;
        int highPair = 0;
        int highTrip = 0;
        for(int i = 0; i < values.length; i++){
            if(values[i] == 2){
                pairCount++;
                values[i] -= 2;
                if(i == 0){
                    test = 100000 + 1000*14 + getHighest(3, values);
                    highPair = 14;
                } else if (i+1 > highPair){
                    test = 100000 + 1000*(i+1) + getHighest(3, values);
                    highPair = i+1;
                }
                values[i] += 2;
            } else if (values[i] == 3){
                values[i] -= 3;
                if(i == 0){
                    test = 300000 + 1000*14 + getHighest(2, values);
                    highTrip = 14;
                } else if (i+1 > highTrip){
                    test = 300000 + 1000*(i+1) + getHighest(2, values);
                    highTrip = i+1;
                }
            }
        }
        if(test > best) best = test;

        if((best < 300000) && (pairCount >= 2)){
            test = 200000;
            int cur = 12;
            int index = 3;
            int temp[] = new int[values.length];
            for(int i = 0; i < values.length; i++){
                temp[i] = values[i];
            }
            while(cur > 0 && index >= 1){
                if(temp[0] == 2){
                    test += Math.pow(10, index) * 14;
                    temp[0] -= 2;
                    index -= 2;
                } else if (temp[cur] == 2){
                    test += Math.pow(10, index) * (cur+1);
                    temp[cur] -= 2;
                    index -= 2;
                } else cur--;
            }
            test += getHighest(1, temp);
        }

        if((highPair != 0) && (highTrip != 0)){
            test = 600000 + highTrip * 1000 + highPair;
        }
        if(test > best) best = test;

        // straight
        if((isStraight(values) != -1) && (best < (400000 + isStraight(values) * 1000))) best = (400000 + isStraight(values) * 1000);

        // flush and straight flush
        for(int i = 0; i < suits.length; i++){
            if((suits[i] >= 5)){
                int[] flushVals = new int[13];
                for(int j = 0; j < pool.size(); j++){
                    if(pool.get(j).getSuit() == i+1) flushVals[pool.get(j).getValue()-1]++;
                }

                if((isStraight(flushVals) != -1) && (best < 800000 + isStraight(flushVals) * 1000)) best = 800000 + isStraight(flushVals) * 1000;
                else if (best < 500000 + getHighest(5, flushVals)) best = 500000 + getHighest(5, flushVals);
            }
        }

        // high card
        if(best < 100000) best = getHighest(5, values);

        return best;
    }

    public int getHighest(int fill, int[] values){
        int sum = 0;
        int cur = 12;
        int temp[] = new int[values.length];
        for(int i = 0; i < values.length; i++){
            temp[i] = values[i];
        }
        while((fill > 0) && (cur > 0)){
            if(temp[0] > 0){
                sum += 14;
                temp[0]--;
                fill--;
            } else {
                if(temp[cur] > 0){
                    sum += cur+1;
                    temp[cur]--;
                    fill--;
                } else cur--;
            }
        }
        return sum;
    }

    public int isStraight(int[] values){
        int count = 1;
        int highest = -1;
        for(int i = 1; i < values.length; i++){
            if((values[i] >= 1) && (values[i-1] >= 1)) count++;
            else count = 1;

            if((count >= 4) && (i == 12) && (values[0] >= 1)){
                return 14;
            } else if (count >= 5){
                highest = i+1;
            }
        }
        return highest;
    }

    public String handText(int hand){
        if(hand < 100000) return "High Card";
        else if(hand < 200000) return "Pair";
        else if(hand < 300000) return "Two Pair";
        else if(hand < 400000) return "Three-of-a-kind";
        else if(hand < 500000) return "Straight";
        else if(hand < 600000) return "Flush";
        else if(hand < 700000) return "Full House";
        else if(hand < 800000) return "Quads";
        else if(hand < 900000) return "Straight Flush";
        return "none";
    }

    public int calculateLosingHands(int playerNum, ArrayList<Card> community, ArrayList<Card> deck){
        int loserCount = 0;
        for(int i = 0; i < deck.size(); i++){
            Card n = deck.get(i);
            for(int j = i+1; j < deck.size(); j++){
                ArrayList<Card> otherHandPool = new ArrayList<Card>();
                Card m = deck.get(j);
                otherHandPool.add(n);
                otherHandPool.add(m);
                for(int k = 0; k < community.size(); k++){
                    otherHandPool.add(community.get(k));
                }
                if(getBestHand(otherHandPool) <= playerNum) loserCount++;

            }
        }
        return loserCount;
    }

    public void printBeaters(int playerNum, ArrayList<Card> community, ArrayList<Card> deck){
        int loserCount = 0;
        int winnerCount = 0;
        for(int i = 0; i < deck.size(); i++){
            Card n = deck.get(i);
            for(int j = i+1; j < deck.size(); j++){
                ArrayList<Card> otherHandPool = new ArrayList<Card>();
                Card m = deck.get(j);
                otherHandPool.add(n);
                otherHandPool.add(m);
                for(int k = 0; k < community.size(); k++){
                    otherHandPool.add(community.get(k));
                }
                if(getBestHand(otherHandPool) <= playerNum) loserCount++;
                else winnerCount++;

            }
        }
        System.out.println("You win over " + loserCount + " hands.");
        System.out.println(winnerCount + " hands beat yours.");
    }
}
