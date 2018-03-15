package com.example.android.blackjack;

import android.os.Debug;
import android.util.Log;

import java.util.Random;

/**
 * Created by Owner on 2/25/2018.
 */



// implement a deck of 52 playing cards
// includes methods to
//      - create the deck of cards
//      - shuffle the deck of cards
//      - get the top card from the deck
//      - print all info for all cards in the deck, in order
public class DeckOfCards {

    public int TOTAL_CARDS = 52;
    public int remaining_cards;                         // how many cards left in the deck
    public int top_card;                                // array position of next card in deck
    public card_t[] cards = new card_t[TOTAL_CARDS];

    public DeckOfCards() {
        int card = 0;

        for (card_t.card_suit suit : card_t.card_suit.values())
        {
            for (card_t.card_rank rank : card_t.card_rank.values())
            {
                cards[card] = new card_t(suit, rank);
                card++;
            }
        }

        remaining_cards = TOTAL_CARDS;
        top_card = 0;
    }

    // used to shuffle the deck
    // swaps the position of two cards
    private void swapCards(card_t card1, card_t card2) {
        card_t.card_suit temp_suit;
        card_t.card_rank temp_rank;
        int temp_val;
        String temp_pic;

        temp_suit = card1.suit;
        temp_rank = card1.rank;
        temp_val = card1.value;
        temp_pic = card1.pic;

        card1.suit = card2.suit;
        card1.rank = card2.rank;
        card1.value = card2.value;
        card1.pic = card2.pic;

        card2.suit = temp_suit;
        card2.rank = temp_rank;
        card2.value = temp_val;
        card2.pic = temp_pic;
    }

    // randomize the order of the cards in the deck by swapping
    // each card with another, random card in the deck
    public void shuffleDeck() {
        int i;
        Random randomNum = new Random();

        for (card_t card : cards) {
            i = randomNum.nextInt(52);
            swapCards(card, cards[i]);
        }

        remaining_cards = TOTAL_CARDS;
        top_card = 0;

        // we can stack the deck here for testing of specific hands
        /*cards[1].rank   = card_t.card_rank.ace;
        cards[1].value  = 11;
        cards[1].suit   = card_t.card_suit.spades;
        cards[1].pic    = "ace_of_spades";
        cards[3].rank = card_t.card_rank.ace;
        cards[3].value = 11;
        cards[3].suit = card_t.card_suit.clubs;
        cards[3].pic = "ace_of_clubs";*/
    }

    // get the next card in the deck, we never actually remove any cards from the deck,
    // instead we just move the index of the `top_card` top the next card in the array
    //
    // currently assuming this can't fail
    public card_t getNextCard() {
        card_t c;

        // if (remaining_cards == 0) return error;

        c = cards[top_card];
        top_card++;
        remaining_cards--;

        return c;
    }

    // print info about each card in the deck, in order
    public void printDeck() {
        for (card_t card : cards) {
            card.printCard();
        }
    }
}
