package me.iologic.apps.dtn;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

public class AudioPlayer extends AppCompatActivity {

    TextView customDesignTxt;
    TextView closeAudioPlayer;
    ImageButton playAudioBtn;

    AudioManager audioManager;
    int playBtnClick;
    boolean startedPlaying;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        customDesignTxt = (TextView) findViewById(R.id.customDesignText);
        closeAudioPlayer = (TextView) findViewById(R.id.closeAudioPlayer);
        playAudioBtn = (ImageButton) findViewById(R.id.playAudio);

        customDesignTxt.setSelected(true);
        customDesignTxt.setHorizontallyScrolling(true);

        audioManager = new AudioManager(this);

        startedPlaying = false;

        // For playing Audio

        playAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playBtnClick % 2 == 0) {
                    playAudio();
                    playBtnClick++;
                } else {
                    if (startedPlaying) {
                        audioManager.pausePlaying();
                    }
                }
            }
        });

        // For closing the Audio Player

        closeAudioPlayer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                closeAudioPlayer();
            }
        });


    }

    private void playAudio() {
        audioManager.onPlay(true);
        playAudioBtn.setImageResource(R.drawable.ic_pause_audio);
        startedPlaying = true;
        Log.i(Constants.TAG, "Audio length:" + audioManager.getAudioFileLength());
    }


    private void closeAudioPlayer() {
        finish();
    }
}
