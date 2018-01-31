package me.iologic.apps.dtn;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by vinee on 16-01-2018.
 */

public class FileServices {

    private File file;
    private FileOutputStream outputStream;
    private Context ctx;

    byte[] readData;

    public FileServices(Context context)
    {
        ctx = context;
    }

    public File createTemporaryFile(String ReceivedFileName) {

        file = new File(ctx.getFilesDir(), ReceivedFileName);
        return file;
    }

    public boolean checkFileExists(String ReceivedFileName){
        File file = new File(ctx.getFilesDir() + File.separator + ReceivedFileName);
        if(file.exists())
        {
            Log.i(Constants.TAG, "File is found!" + file.getName());
            return true;
        }
            Log.e(Constants.TAG, "File not found");
            return false;
    }

    public File returnFile(String ReceivedFileName){
        File file = new File(ctx.getFilesDir(), ReceivedFileName);
        if(file.exists()){
            return file;
        } else {
            return null;
        }

    }



    public void fillTempFile(File ReceivedTempFileObj)
    {
        try {
            RandomAccessFile f = new RandomAccessFile(ReceivedTempFileObj, "rw");
            f.setLength(1024 * 1024);
        } catch (IOException FileError){
            Log.i(Constants.TAG, "Could Not Read File");
        }

    }

    public byte[] readTempFile(File ReceivedFileObj){
        byte [] data = new byte[ (int) ReceivedFileObj.length() ];
        readData = new byte[(int) ReceivedFileObj.length()];
        try {
            FileInputStream fin = new FileInputStream(ReceivedFileObj);
            int n = 0;
            while ( (n = fin.read(data, n, data.length - n) ) > 0);

        }
        catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "File not found (from read() file): " + e.toString());
        } catch (IOException e) {
            Log.e(Constants.TAG, "Can not read file: " + e.toString());
        }

        readData = data;

        Log.i(Constants.TAG, "File Size Read:" + readData.length);

        return readData;
    }

    public long getFileSize(){
        return readData.length;
    }

    static String createString(long size){
        StringBuilder o=new StringBuilder();
        for(int i=0;i<size;i++){
            o.append("*");
        }
        return o.toString();
    }

    public void deleteFile(){
          if(file.delete() == true)
          {
            Log.i(Constants.TAG, "File " + file.getName() + " is deleted.");
          } else {
              Log.e(Constants.TAG, "File " + file.getName() + " could not be deleted.");
          }
    }
}
