package com.example.android.blackjack;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    TextView titleText;
    Button startButton;

    boolean playMusic = true;
    MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initUIRefs();
    }

    private void initUIRefs() {

        titleText = findViewById(R.id.titleText);
        startButton = findViewById(R.id.startButton);

        // set font for results text view
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/PokerKings-Regular.ttf");
        titleText.setTypeface(custom_font);
        startButton.setTypeface(custom_font);
    }

    public void startGame(View view) {
        if (playMusic) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.intro);
            mediaPlayer.start();
        }

        Intent intent = new Intent(this, BlackJack.class);

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}