package me.iologic.apps.dtn;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioManager {

    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private static String mFileName;

    public AudioManager()

    {
        mRecorder = null;
        mPlayer = null;
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }


    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(generateAudioFileName());
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(Constants.TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(generateAudioFileName());
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(Constants.TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private String generateAudioFileName(){
        mFileName = randomFileNameGenerator(5) + ".3gp";
        return mFileName;
    }

    public static String randomFileNameGenerator(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * Constants.Miscellaneous.ALPHA_NUMERIC_STRING.length());
            builder.append(Constants.Miscellaneous.ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
