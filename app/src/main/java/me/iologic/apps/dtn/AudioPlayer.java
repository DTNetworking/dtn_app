package me.iologic.apps.dtn;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

public class AudioPlayer extends AppCompatActivity {

    TextView customDesignTxt;
    TextView closeAudioPlayer;
    ImageButton playAudioBtn;
    SeekBar audioSeekBar;

    AudioManager audioManager;
    MediaPlayer mAudioPlayer;
    private boolean isAudioPlaying;
    private boolean ifresumeAudio;
    private int songLength;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        customDesignTxt = (TextView) findViewById(R.id.customDesignText);
        closeAudioPlayer = (TextView) findViewById(R.id.closeAudioPlayer);
        playAudioBtn = (ImageButton) findViewById(R.id.playAudio);
        audioSeekBar = (SeekBar) findViewById(R.id.audioSeekBar);

        customDesignTxt.setSelected(true);
        customDesignTxt.setHorizontallyScrolling(true);

        audioManager = new AudioManager(this);

        mAudioPlayer = audioManager.getmAudioPlayer();

        isAudioPlaying = false;
        ifresumeAudio = false;

        // For playing Audio

        playAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAudioPlaying & !ifresumeAudio) {
                    playAudio();
                } else if (isAudioPlaying) {
                    pauseAudio();
                } else if (!isAudioPlaying & ifresumeAudio) {
                    resumeAudio();
                }

                if (mAudioPlayer != null) {

                    mAudioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Log.i(Constants.TAG, "Song Completed!");
                            audioManager.onPlay(false);
                            playAudioBtn.setImageResource(R.drawable.ic_play_audio);
                            isAudioPlaying = false;
                            ifresumeAudio = false;
                        }
                    });

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

    Thread seekBarAudioTracking = new Thread() {
        @Override
        public void run() {
            int currentPosition = 0;
            while (currentPosition < songLength) {
                try {
                    Thread.sleep(1000);
                    currentPosition = mAudioPlayer.getCurrentPosition();
                    Log.i(Constants.TAG, "dnfldfn");
                } catch (InterruptedException e) {
                    Log.i(Constants.TAG, "Could not start");
                    return;
                }
                audioSeekBar.setProgress(mAudioPlayer.getCurrentPosition());
            }
        }
    };


    private void playAudio() {
        audioManager.onPlay(true);
        playAudioBtn.setImageResource(R.drawable.ic_pause_audio);
        isAudioPlaying = true;
        songLength = audioManager.getAudioFileLength();
        audioSeekBar.setMax(songLength);
        // Log.i(Constants.TAG, "Audio length:" + audioManager.getAudioFileLength());
        if (!seekBarAudioTracking.isAlive()) {
            Log.i(Constants.TAG, "Thread Starting");
            seekBarAudioTracking.start();
        }
    }

    private void pauseAudio() {
        playAudioBtn.setImageResource(R.drawable.ic_play_audio);
        audioManager.pausePlaying();
        isAudioPlaying = false;
        ifresumeAudio = true;
    }

    private void resumeAudio() {
        playAudioBtn.setImageResource(R.drawable.ic_pause_audio);
        audioManager.resumePlaying();
        isAudioPlaying = true;
    }


    private void closeAudioPlayer() {
        finish();
    }
}
