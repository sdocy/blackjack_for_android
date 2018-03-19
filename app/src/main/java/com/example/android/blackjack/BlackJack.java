package com.example.android.blackjack;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Handler;
import android.widget.Toast;


public class BlackJack extends AppCompatActivity {


    // constants
    static int TWENTY_ONE = 21;
    static int MAX_CARDS_PER_HAND = 12;
    static int DEAL_DELAY = 700;
    static int DEALER_TURN_DELAY = 1200;
    static int INITIAL_CASH = 1000;
    static int INITIAL_BET = 100;
    static int BET_ADJUST = 50;

    // UI references
    Button dealButton;
    Button hitButton;
    Button standButton;
    Button doubleDownButton;
    Button incBetButton;
    Button decBetButton;
    ImageButton imageButton;

    // sound fx
    boolean playSounds = true;
    CheckBox playSoundsBox;
    SoundPool sounds;
    int soundShuffle;
    int soundCardDrop;
    int soundDing;
    int soundPop;
    int soundLose;
    int soundCheer;

    // expand/collapse results layout
    LinearLayout.LayoutParams closedParams =
            new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
    LinearLayout.LayoutParams openParams =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    DeckOfCards Deck = new DeckOfCards();
    Player thePlayer = new Player();
    Player theDealer = new Player();

    int playerCash = INITIAL_CASH;          // how much cash the player has
    int playerBet = INITIAL_BET;            // how much the player bets each hand
    int amountWon;                          // amount player wins (or gets back for push)
    TextView playerCashText;                // display player's cash
    TextView playerBetText;                 // disaply player's bet
    TextView playerCashTextTag;             // display "Total " tag
    TextView playerBetTextTag;              // display "Bet " tag

    // shows results (win, lose, tie)
    TextView resultsText;                   // shows win. lose. balckjack, etc.
    LinearLayout resultsLayout;             // pointer to layout for `resultsText` so we can dynamically expand
                                            // it to show the results once the hand is done
    boolean showSettings = false;           // are the settings views visible?


    // set when a player doubles down, adding more to their bet amount
    int doubleDownBet;

    // hidden dealer card
    card_t dealerHoleCard;

    // do we show the total card count on the screen?
    CheckBox showCardTotalsBox;
    boolean showCardTotals = false;

    // stop runnables that do things like flashing text when a new hand is dealt
    boolean stopRunnable;
    boolean resultsOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_jack);
        this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // get UI refs
        initUIRefs();

        // enable / disable appropriate buttons
        initButtons();

        // init hand variables
        init();

        // setup settings ImageButton listener
        addListenerOnButton();
    }

    // reset state for start of a new hand
    void init() {
        // reset hands stats for new hand
        thePlayer.resetState();
        theDealer.resetState();

        // stop runnables that do things like flash results text
        stopRunnable = true;

        doubleDownBet = 0;

        for (ImageView img : thePlayer.cardImages) {
            img.setImageResource(R.drawable.green_card);
        }

        for (ImageView img : theDealer.cardImages) {
            img.setImageResource(R.drawable.green_card);
        }

        // hide reuslts
        resultsText.setText("");
        resultsLayout.setLayoutParams(closedParams);
        resultsText.setAlpha(1.0f);         // in case it was flashing off when new hand was dealt
    }

    // reset button states for start of new hand
    void initButtons() {
        enableDeal();
        enableBetAdjust();
        disableHitStand();
        disableDoubleDown();
    }

    // get references to XML button, text and image views
    void initUIRefs() {
        // get button references
        dealButton = findViewById(R.id.dealButton);
        hitButton = findViewById(R.id.hitButton);
        standButton = findViewById(R.id.standButton);
        doubleDownButton = findViewById(R.id.doubleDownButton);
        incBetButton = findViewById(R.id.incBetButton);
        decBetButton = findViewById(R.id.decBetButton);
        imageButton = findViewById(R.id.settingsButton);

        // get total cash and bet references
        playerCashText = findViewById(R.id.playerCash);
        playerBetText = findViewById(R.id.playerBet);
        playerCashTextTag = findViewById(R.id.playerCashTag);
        playerBetTextTag = findViewById(R.id.playerBetTag);
        thePlayer.playerCardTotalText = findViewById(R.id.playerCardTotalText);
        theDealer.playerCardTotalText = findViewById(R.id.dealerCardTotalText);

        // card totals are initially invisible
        thePlayer.playerCardTotalText.setAlpha(0.0f);
        theDealer.playerCardTotalText.setAlpha(0.0f);

        // set initial cash and bet amounts
        playerCashText.setText("$" + playerCash);
        playerBetText.setText("$" + playerBet);

        // get reference to results text view
        resultsText = findViewById(R.id.resultsText);
        resultsLayout = findViewById(R.id.resultsLayout);

        // get reference to settings views
        showCardTotalsBox = findViewById(R.id.showCardTotalsBox);
        showCardTotalsBox.setVisibility(View.INVISIBLE);
        playSoundsBox = findViewById(R.id.playSoundsBox);
        playSoundsBox.setVisibility(View.INVISIBLE);
        playSoundsBox.setChecked(true);

        // set font
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/PokerKings-Regular.ttf");
        resultsText.setTypeface(custom_font);
        dealButton.setTypeface(custom_font);
        hitButton.setTypeface(custom_font);
        standButton.setTypeface(custom_font);
        doubleDownButton.setTypeface(custom_font);
        playerCashTextTag.setTypeface(custom_font);
        playerBetTextTag.setTypeface(custom_font);
        showCardTotalsBox.setTypeface(custom_font);
        playSoundsBox.setTypeface(custom_font);

        // get card image view refs for the 12 player card ImageViews and 12 dealer card ImageViews
        for (int i = 0; i < MAX_CARDS_PER_HAND; i++) {
            String pCard = "playerCard" + i;
            String dCard = "dealerCard" + i;

            // use string name to get resource id
            int pCardID = getResources().getIdentifier(pCard, "id", getPackageName());
            if (pCardID == 0) {
                Log.v("ERROR", "initUIRefs() failed to get pCardID for " + pCard);
            }
            thePlayer.cardImages[i] = findViewById(pCardID);
            if (thePlayer.cardImages[i] == null) {
                Log.v("ERROR", "initUIRefs() failed to get pCard[" + i + "] reference");
            }

            int dCardID = getResources().getIdentifier(dCard, "id", getPackageName());
            if (dCardID == 0) {
                Log.v("ERROR", "initUIRefs() failed to get dCardID for " + dCard);
            }
            theDealer.cardImages[i] = findViewById(dCardID);
            if (theDealer.cardImages[i] == null) {
                Log.v("ERROR", "initUIRefs() failed to get dCard[" + i + "] reference");
            }
        }

        // sfx pool
        sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundShuffle = sounds.load(this, R.raw.shuffle, 1);
        soundCardDrop = sounds.load(this, R.raw.carddrop, 1);
        soundDing = sounds.load(this, R.raw.ding, 1);
        soundPop = sounds.load(this, R.raw.pop, 1);
        soundLose = sounds.load(this, R.raw.lose, 1);
        soundCheer = sounds.load(this, R.raw.cheer, 1);
    }

    // toggle settings view
    public void addListenerOnButton() {
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (showSettings) {
                    // settings are currently visible, hide them
                    playSoundsBox.setVisibility(View.INVISIBLE);
                    showCardTotalsBox.setVisibility(View.INVISIBLE);
                } else {
                    // settings are currently hidden, show them
                    playSoundsBox.setVisibility(View.VISIBLE);
                    showCardTotalsBox.setVisibility(View.VISIBLE);
                }

                showSettings = !showSettings;
            }
        });
    }


    // called when the user hits the "DEAL" button, deal two cards to the player and two cards to
    // the dealer (one face up, one face down)
    public void deal(View view) {
        if (playSounds) {
            sounds.play(soundShuffle, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        // disable buttons before creating a thread because we don't want a delay before doing it
        // and we need it to be executed on the UI thread
        disableAll();

        // reset hand state for a new hand
        init();

        // deduct bet amount from player's cash
        subtractBet();

        // create a new thread to execute all the code that I want to include
        // synchronous delays
        new Thread() {
            public void run() {
                doDelay(DEAL_DELAY);
                // deal player their first card
                // anything that updates on-screen elements must be run on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Deck.shuffleDeck();
                        //Deck.printDeck();
                    }
                });

                doDelay(DEAL_DELAY);
                // deal player their first card
                // anything that updates on-screen elements must be run on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dealCardToPlayer(thePlayer);
                    }
                });

                doDelay(DEAL_DELAY);
                // deal first card to dealer
                // anything that updates on-screen elements must be run on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dealCardToPlayer(theDealer);
                    }
                });

                doDelay(DEAL_DELAY);
                // deal player their second card
                // anything that updates on-screen elements must be run on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dealCardToPlayer(thePlayer);
                    }
                });

                doDelay(DEAL_DELAY);
                // deal hole card to dealer
                // anything that updates on-screen elements must be run on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dealHoleCardToDealer();

                        // anyone get dealt blackjack?
                        if (!checkForBJ()) {
                            enableHitStand();

                            if ((thePlayer.cardTotal == 9) || (thePlayer.cardTotal == 10) || (thePlayer.cardTotal == 11)) {
                                if (playerCash >= playerBet) {
                                    enableDoubleDown();
                                }
                            }
                        }
                    }
                });
            }
        }.start();
    }

    // player clicked "HIT"
    // deal them another card
    public void hit(View view) {
        // can't doubleDown after taking another card
        disableDoubleDown();

        dealCardToPlayer(thePlayer);

        // see if player busted
        if (thePlayer.cardTotal > TWENTY_ONE) {
            disableHitStand();
            busted();
        }
    }

    // player clicked 'STAND'
    // it is now the dealer's turn
    public void stand(View view) {
        disableAll();

        dealersTurn();
    }

    // player clicked "DOUBLE DOWN"
    // deal the player one card, and then it is dealer's turn
    public void doubleDown(View view) {
        disableAll();

        // subtract another bet since doubling down doubles the player's bet
        subtractBet();
        doubleDownBet = playerBet;

        dealCardToPlayer(thePlayer);

        dealersTurn();
    }

    // called when player decreases bet amount
    public void decrement(View view) {
        if (playSounds) {
            sounds.play(soundPop, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        playerBet -= BET_ADJUST;

        if (playerBet <=0) {
            playerBet = BET_ADJUST;
        }

        // make sure player has enough cash to cover the bet,
        // adjusting if necessary
        checkBetAmount();

        playerBetText.setText("$" + playerBet);
    }

    // called when player increases bet amount
    public void increment(View view) {
        if (playSounds) {
            sounds.play(soundPop, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        playerBet += BET_ADJUST;

        // make sure player has enough cash to cover the bet,
        // adjusting if necessary
        checkBetAmount();

        playerBetText.setText("$" + playerBet);
    }

    // does the player want to see card totals on the screen?
    // called when checkbox is checked / unchecked
    public void toggleShowCardTotals(View view) {
        if (playSounds) {
            sounds.play(soundPop, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        showCardTotals = showCardTotalsBox.isChecked();

        if (showCardTotals) {
            thePlayer.playerCardTotalText.setAlpha(1.0f);
            theDealer.playerCardTotalText.setAlpha(1.0f);
        } else {
            thePlayer.playerCardTotalText.setAlpha(0.0f);
            theDealer.playerCardTotalText.setAlpha(0.0f);
        }
    }

    // does the user want sound fx on?
    public void togglePlaySounds(View view) {
        playSounds = playSoundsBox.isChecked();

        if (playSounds) {
            sounds.play(soundPop, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    // introduce a delay into a non-UI thread
    void doDelay(int delay) {
        try {
            Thread.sleep(delay);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    // subtract player bet amount from player total cash and update screen text
    // we have already validated that the player has enough cash for the bet
    void subtractBet() {
        playerCash -= playerBet;
        playerCashText.setText("$" + playerCash);
    }

    // make sure player has enough cash to cover the bet, adjusting bet size if needed
    void checkBetAmount() {
        if (playerBet > playerCash) {
            playerBet = playerCash;
        }
    }

    // get next card from the deck and deal it to the specifed `player`
    void dealCardToPlayer(Player player) {
        card_t card;

        card = Deck.getNextCard();
        placePlayerCard(card, player);

        if (player.cardTotal > TWENTY_ONE) {
            // must have been dealt two aces
            checkForAceToLower(player);
        }
    }

    // store next card for dealer's hole card, will reveal it after player's turn
    void dealHoleCardToDealer() {
        if (playSounds) {
            sounds.play(soundCardDrop, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        dealerHoleCard = Deck.getNextCard();

        showCard(theDealer.cardImages[1], "back");
    }

    // Called when the player is dealt a card
    //      - update's player card total, adding value of new card
    //      - displays card image in next available player card image spot
    void placePlayerCard(card_t card, Player player) {
        if (playSounds) {
            sounds.play(soundCardDrop, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        player.cardTotal += card.value;

        // update displayable card total
        player.playerCardTotalText.setText("" + player.cardTotal);

        showCard(player.cardImages[player.cardPos], card.pic);
        player.cardPos++;

        player.numCards++;

        // track number of aces the player has been dealt, so we can change the
        // value of an ace from 11 to 1 if they bust
        if (card.rank == card_t.card_rank.ace) {
            player.numAces++;
        }
    }

    // update screen with image of new card
    void showCard(ImageView img, String cardImage) {
        int cardImgID = getResources().getIdentifier(cardImage, "drawable", getPackageName());

        if (cardImgID == 0) {
            Log.v("ERROR", "showCard() failed to get cardImgID for " + cardImage);
        }

        img.setImageResource(cardImgID);
    }

    // Show the hole card that was dealt to the dealer
    // and add its value to the dealer total.
    void revealHoleCard() {
        theDealer.cardTotal += dealerHoleCard.value;

        // update displayable card total
        theDealer.playerCardTotalText.setText("" + theDealer.cardTotal);

        showCard(theDealer.cardImages[theDealer.cardPos], dealerHoleCard.pic);
        theDealer.cardPos++;

        theDealer.numCards++;

        // track number of aces the dealer has been dealt, so we can change the
        // value of an ace from 11 to 1 if he busts
        if (dealerHoleCard.rank == card_t.card_rank.ace) {
            theDealer.numAces++;
        }
    }



    // If the player has not busted, the dealer plays
    // according to standard blackjack rules, drawing
    // cards until getting 17 or more, or busting.  Dealer
    // hits on soft 17, a total of 17 with an ace which is
    // currently counted as 11.
    //
    // Keep in sync with doDealersTurnSim()
    void dealersTurn() {
        // create a new thread to execute all the code that I want to introduce
        // synchronous delays between
        new Thread() {
            public void run() {
                doDelay(DEALER_TURN_DELAY);

                // play sound for revealing dealer's hole card
                if (playSounds) {
                    sounds.play(soundCardDrop, 1.0f, 1.0f, 0, 0, 1.0f);
                }

                // reveal dealer's hole card
                // anything that updates on-screen elements must be run on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        revealHoleCard();

                        if (theDealer.cardTotal > TWENTY_ONE) {
                            // must have been dealt two aces
                            checkForAceToLower(theDealer);
                        }
                    }
                });

                doDelay(DEAL_DELAY);
                while ((theDealer.cardTotal < 17) || ((theDealer.cardTotal == 17) && (theDealer.numAces > 0))) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dealCardToPlayer(theDealer);
                        }
                    });

                    doDelay(DEAL_DELAY);
                }

                if (theDealer.cardTotal > TWENTY_ONE) {
                    // we don't update Text, just internal value
                    // set to -1 so playerTotal will be greater then dealerTotal
                    theDealer.cardTotal = -1;
                }

                doDelay(DEAL_DELAY);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seeWhoWins(false);
                    }
                });
            }
        }.start();
    }


    // We have over 21, see if we have an ace we can change from 11 to 1.
    // Handles special case, player has an ace, has 21, hits and gets another
    // aced, now has 32, demoting an ace still leaves 22, need to demote the other ace
    void checkForAceToLower(Player player) {
        if (player.numAces > 0) {
            player.numAces--;
            player.cardTotal = player.cardTotal - 10;

            // update displayable card total
            player.playerCardTotalText.setText("" + player.cardTotal);

            if (player.cardTotal > TWENTY_ONE) {
                checkForAceToLower(player);
            }
        }
    }

    // see if player or dealer has been dealt blackjack
    boolean checkForBJ() {
        if ((thePlayer.cardTotal == TWENTY_ONE) || ((theDealer.cardTotal + dealerHoleCard.value) == TWENTY_ONE)) {
            revealHoleCard();
            seeWhoWins(true);
            return true;
        }
        else {
            return false;
        }
    }

    // player lost, show result and enable DEAl and betting buttons
    void playerLoses(String msg) {
        if (playSounds) {
            sounds.play(soundLose, 1.0f, 1.0f, 0, 0, 1.0f);
        }

        resultsText.setText(msg);
        resultsLayout.setLayoutParams(openParams);

        enableDeal();
        enableBetAdjust();
    }

    // player and dealer have same amount, return player's bet and show result
    // DEAL button is enabled by updateCash() after it finishes tallying player's cash
    void push() {
        // player keeps their bet
        amountWon = playerBet + doubleDownBet;
        updateCash();

        resultsText.setText(getResources().getString(R.string.push));
        resultsLayout.setLayoutParams(openParams);
    }

    // player won, payoff player's bet and show result
    // DEAL button is enabled by updateCash() after it finishes tallying player's cash
    void playerWins(boolean bj) {
        if (bj) {
            if (playSounds) {
                sounds.play(soundCheer, 1.0f, 1.0f, 0, 0, 1.0f);
            }

            amountWon = (int)(2.5 * (playerBet + doubleDownBet));

            resultsText.setText(getResources().getString(R.string.blackjack));
            flashResults();
        } else {
            amountWon = (2 * (playerBet + doubleDownBet));

            resultsText.setText(getResources().getString(R.string.youWin));
        }

        updateCash();

        resultsLayout.setLayoutParams(openParams);
    }

    // Determine the results of the hand.
    // `blackjack` let's us know if we came here after we
    // determined that the player hit blackjack.  It is used in
    // playerWins() to pay the appropriate 1.5 to 1 and display
    // the correct winning message
    void seeWhoWins(boolean blackjack) {
        disableAll();

        if (thePlayer.cardTotal > TWENTY_ONE) {
            playerLoses(getResources().getString(R.string.busted));
        } else if (thePlayer.cardTotal > theDealer.cardTotal) {
            playerWins(blackjack);
        } else if (theDealer.cardTotal > thePlayer.cardTotal) {
            playerLoses(getResources().getString(R.string.dealerWins));
        } else {
            push();
        }
    }

    // Player has 'hit' and gone over 21
    void busted() {
        // have dealer reveal his hole card
        revealHoleCard();
        seeWhoWins(false);
    }


    // handler to flash results on and off
    // used when player gets blackjack
    Handler handler = new Handler();
    final Runnable r = new Runnable() {
        public void run() {
            // make sure stopRunnable wasn;t set while we were
            // waiting to execute this
            if (stopRunnable) {
                return;
            }

            if (resultsOn) {
                resultsText.setAlpha(1.0f);
            } else {
                resultsText.setAlpha(0.0f);
            }

            // toggle so we flash on and off
            resultsOn = !resultsOn;

            if (!stopRunnable)
                handler.postDelayed(this, 100);
        }
    };
    // called when player gets blackjack, to flash the results message on and off
    void flashResults() {
        stopRunnable = false;
        handler.postDelayed(r, 100);
    }

    // handler to add money to player's cash total in an animated manner
    Handler cashHandler = new Handler();
    final Runnable cashRunnable = new Runnable() {
        public void run() {
            if (playSounds) {
                sounds.play(soundDing, 0.2f, 0.2f, 0, 0, 1.0f);
            }

            // going up in increments of 10 results in a good
            // time to update, but handle individual updates of 1
            // just in case bet is not a multiple of 10 (should be
            // a rare but possible case)
            if (amountWon >= 10) {
                amountWon -= 10;
                playerCash +=10;
                playerCashText.setText("$" + playerCash);

                cashHandler.postDelayed(this, 1);
            } else if (amountWon > 0) {
                amountWon--;
                playerCash++;
                playerCashText.setText("$" + playerCash);

                cashHandler.postDelayed(this, 1);
            } else {
                enableDeal();
                enableBetAdjust();
            }
        }
    };
    void updateCash() {
        cashHandler.postDelayed(cashRunnable, 1);
    }



    // turn on/off specific buttons
    void enableDeal() {
        dealButton.setEnabled(true);

        // make sure player has enough cash to cover his current bet
        checkBetAmount();
        playerBetText.setText("$" + playerBet);
    }
    void disableDeal() {
        dealButton.setEnabled(false);
    }
    void enableHitStand() {
        hitButton.setEnabled(true);
        standButton.setEnabled(true);
    }
    void disableHitStand() {
        hitButton.setEnabled(false);
        standButton.setEnabled(false);
    }
    void enableDoubleDown() {
        doubleDownButton.setEnabled(true);
    }
    void disableDoubleDown() {
        doubleDownButton.setEnabled(false);
    }
    void enableBetAdjust() {
        incBetButton.setEnabled(true);
        decBetButton.setEnabled(true);
    }
    void disableBetAdjust() {
        incBetButton.setEnabled(false);
        decBetButton.setEnabled(false);
    }
    void disableAll() {
        disableDeal();
        disableHitStand();
        disableDoubleDown();
        disableBetAdjust();
    }
}
