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
        OutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(fullPath));
        InputStream inputStream = new ByteArrayInputStream(bytes);
        int token = -1;

        while ((token = inputStream.read()) != -1) {
            bufferedOutputStream.write(token);
        }
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        inputStream.close();
    }
}