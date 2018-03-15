package com.example.android.blackjack;

import android.content.res.Resources;
import android.util.Log;

/**
 * Created by Owner on 2/25/2018.
 */

// implement a standard playing card
public class card_t {

    public enum card_suit { clubs, diamonds, hearts, spades };
    public enum card_rank { two, three, four, five, six, seven, eight, nine, ten, jack, queen, king, ace };

    public card_suit suit;      // clubs, diamonds, hearts, spades
    public card_rank rank;      // two, three, four, five, six, seven, eight, nine, ten, jack, queen, king, ace
    public int value;           // 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
    public String pic;          // playing card image name

    public card_t(card_suit in_suit, card_rank in_rank) {
        suit = in_suit;
        rank = in_rank;
        value = getCardValue(rank);
        pic = rank + "_of_" + suit;
    }

    private int getCardValue(card_rank rank) {
        switch (rank) {
            case two: return 2;
            case three: return 3;
            case four: return 4;
            case five: return 5;
            case six: return 6;
            case seven: return 7;
            case eight: return 8;
            case nine: return 9;
            case ten:
            case jack:
            case queen:
            case king: return 10;
            case ace: return 11;
        }

        return -1;
    }

    // print all info about a card (rank, suit, image name)
    public void printCard() {
        Log.v("ERROR", pic);
    }
}
