package me.iologic.apps.dtn;

import android.net.Uri;
import android.provider.MediaStore;
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
import java.net.URI;
import java.net.URISyntaxException;

public class ImageData {

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private long ImageSize;

    public byte[] ImageToBytes(String filePath) {
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
        String fullImagePath = fullPath + "/img000.jpg";
        String renameImagePath = fullPath + File.separator + randomFileNameGenerator(5) + ".jpg";
        Log.i(Constants.TAG, "renameImagePath: " + renameImagePath + " fullImagePath: " + fullImagePath);
        int token = -1;

        File file = new File(fullImagePath);
        File newFile = new File(renameImagePath);

        if (file.exists()) {
            file.renameTo(newFile);
        }

        OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fullImagePath));
        InputStream inputStream = new ByteArrayInputStream(bytes);

        while ((token = inputStream.read()) != -1) {
            bufferedOutputStream.write(token);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        inputStream.close();
    }

    public static String randomFileNameGenerator(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}