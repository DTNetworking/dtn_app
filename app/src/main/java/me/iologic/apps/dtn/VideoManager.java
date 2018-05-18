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

public class VideoManager {

    public byte[] VideoToBytes(String filePath) {
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
    public void writeFileAsBytes(String fullPath, byte[] bytes) throws IOException {
        String fullVideoPath = fullPath + "/video000.mp4";
        String renameVideoPath = fullPath + File.separator + randomFileNameGenerator(5) + ".mp4";
        Log.i(Constants.TAG, "renameVideoPath: " + renameVideoPath + " fullVideoPath: " + fullVideoPath);
        int token = -1;

        File file = new File(fullVideoPath);
        File newFile = new File(renameVideoPath);

        if (file.exists()) {
            file.renameTo(newFile);
        }

        OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fullVideoPath));
        InputStream inputStream = new ByteArrayInputStream(bytes);

        while ((token = inputStream.read()) != -1) {
            bufferedOutputStream.write(token);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        inputStream.close();
    }


    private static String randomFileNameGenerator(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * Constants.Miscellaneous.ALPHA_NUMERIC_STRING.length());
            builder.append(Constants.Miscellaneous.ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
