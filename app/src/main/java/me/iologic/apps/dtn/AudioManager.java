package me.iologic.apps.dtn;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AudioManager {

    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private static String mFileName;
    private Context ctx;

    public AudioManager(Context receviedCTX)

    {
        mRecorder = null;
        mPlayer = null;

        ctx = receviedCTX;
    }

    //Future APIs

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

    public byte[] AudioToBytes(String filePath) {
        File file = new File(filePath);
        Log.i(Constants.TAG, "File Paths: " + filePath);

        if (!file.equals(null)) {
            //init array with file length
            byte[] bytesArray = new byte[(int) file.length()];

            FileInputStream fis;

            try {
                fis = new FileInputStream(file);
                try {
                    fis.read(bytesArray); //read file into bytes[]
                    fis.close();
                } catch (IOException er) {

                }

            } catch (FileNotFoundException io) {

            }

            return bytesArray;
        }

        return null;
    }

    /**
     * Write an array of bytes to a file. Presumably this is binary data; for plain text
     * use the writeFile method.
     */
    public static void writeFileAsBytes(String fullPath, byte[] bytes) throws IOException {
        String fullAudioPath = fullPath + "/audio000.3gp";
        String renameAudioPath = fullPath + File.separator + randomFileNameGenerator(5) + ".3gp";
        Log.i(Constants.TAG, "renameAudioPath: " + renameAudioPath + " fullAudioPath: " + fullAudioPath);
        int token = -1;

        File file = new File(fullAudioPath);
        File newFile = new File(renameAudioPath);

        if (file.exists()) {
            file.renameTo(newFile);
        }

        OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fullAudioPath));
        InputStream inputStream = new ByteArrayInputStream(bytes);

        while ((token = inputStream.read()) != -1) {
            bufferedOutputStream.write(token);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        inputStream.close();
    }

    private String generateAudioFileName(){
        mFileName = Environment.DIRECTORY_MUSIC + File.separator + randomFileNameGenerator(5) + ".3gp";
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
