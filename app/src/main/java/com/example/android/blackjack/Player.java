package com.example.android.blackjack;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Owner on 2/26/2018.
 */

public class Player {

    // next available slot on table to place a card
    public int cardPos;

    // number of card they have been dealt
    public int numCards;

    // number of aces they have ben dealt
    // used to demote aces from 11 to 1 if needed
    public int numAces;

    // card total
    public int cardTotal;
    public TextView playerCardTotalText;            // display player card total

    public ImageView[] cardImages;

    Player() {
        cardPos = 0;
        numCards = 0;
        numAces = 0;
        cardTotal = 0;
        cardImages = new ImageView[12];
    }

    public void resetState() {
        cardPos = 0;
        numCards = 0;
        numAces = 0;
        cardTotal = 0;
        playerCardTotalText.setText("");
    }
}
