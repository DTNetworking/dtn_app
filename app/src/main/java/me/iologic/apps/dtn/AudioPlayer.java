package me.iologic.apps.dtn;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AudioPlayer extends AppCompatActivity {

    TextView audioStatusTxt;
    TextView customDesignTxt;
    TextView closeAudioPlayer;
    TextView currentPlayerTime;
    ImageButton playAudioBtn;
    SeekBar audioSeekBar;

    Animation animFadeIn;

    AudioManager audioManager;
    MediaPlayer mAudioPlayer;
    private boolean isAudioPlaying;
    private boolean ifresumeAudio;
    private int songLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_player);

        audioStatusTxt = (TextView) findViewById(R.id.audioCurrentStatus);
        customDesignTxt = (TextView) findViewById(R.id.customDesignText);
        closeAudioPlayer = (TextView) findViewById(R.id.closeAudioPlayer);
        currentPlayerTime = (TextView) findViewById(R.id.audioCurrentTime);
        playAudioBtn = (ImageButton) findViewById(R.id.playAudio);
        audioSeekBar = (SeekBar) findViewById(R.id.audioSeekBar);

        customDesignTxt.setSelected(true);
        customDesignTxt.setHorizontallyScrolling(true);

        audioManager = new AudioManager(this);

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);

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

        audioSeekBarChangeListener();

    }


    private void playAudio() {
        audioManager.onPlay(true);
        playAudioBtn.startAnimation(animFadeIn);
        playAudioBtn.setImageResource(R.drawable.ic_pause_audio);
        isAudioPlaying = true;
        songLength = audioManager.getAudioFileLength();
        audioSeekBar.setMax(songLength);
        audioStatusTxt.startAnimation(animFadeIn);
        audioStatusTxt.setSingleLine();
        audioStatusTxt.setSelected(true);
        audioStatusTxt.setHorizontallyScrolling(true);
        audioStatusTxt.setText("Playing Audio: audio000.mp3");
        mAudioPlayer = audioManager.getmAudioPlayer();
        // Log.i(Constants.TAG, "Audio length:" + audioManager.getAudioFileLength());
        if (!seekBarAudioTracking.isAlive()) {
            Log.i(Constants.TAG, "Thread Starting");
            seekBarAudioTracking.start();
        }
    }

    private void pauseAudio() {
        playAudioBtn.startAnimation(animFadeIn);
        playAudioBtn.setImageResource(R.drawable.ic_play_audio);
        audioManager.pausePlaying();
        audioStatusTxt.startAnimation(animFadeIn);
        audioStatusTxt.setText("Audio Paused");
        isAudioPlaying = false;
        ifresumeAudio = true;
    }

    private void resumeAudio() {
        playAudioBtn.startAnimation(animFadeIn);
        playAudioBtn.setImageResource(R.drawable.ic_pause_audio);
        audioManager.resumePlaying();
        isAudioPlaying = true;
        audioStatusTxt.setText("Playing Audio: audio000.mp3");
    }


    private void closeAudioPlayer() {
        audioManager.onPlay(false);
        finish();
    }

    public void Timer() {
        long s = TimeUnit.MILLISECONDS.toSeconds(audioManager.getPlayerCurrentPosition()) % 60;
        long m = (TimeUnit.MILLISECONDS.toSeconds(audioManager.getPlayerCurrentPosition()) / 60) % 60;
        String ms = String.format("%02d:%02d", m, s);
        currentPlayerTime.setText(ms);
    }

    public void setTimerPos(int currentSeekBarPos) {
        long s = TimeUnit.MILLISECONDS.toSeconds(currentSeekBarPos) % 60;
        long m = (TimeUnit.MILLISECONDS.toSeconds(currentSeekBarPos) / 60) % 60;
        String ms = String.format("%02d:%02d", m, s);
        currentPlayerTime.setText(ms);
    }

    public void audioSeekBarChangeListener() {
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.SeekToCurrentPosition(i);
                setTimerPos(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
                    if (audioManager.getmAudioPlayer() != null) {
                        currentPosition = audioManager.getPlayerCurrentPosition();
                        audioSeekBar.setProgress(currentPosition);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Timer();
                            }
                        });
                        Log.i(Constants.TAG, "Current Position:" + currentPosition + " SongLength: " + songLength);
                    }
                } catch (InterruptedException e) {
                    Log.i(Constants.TAG, "Could not start");
                    return;
                } catch (IllegalStateException e) {
                    Log.i(Constants.TAG, "mAudioPlayer is null");
                    return;
                }
            }
        }
    };
}
