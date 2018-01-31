package me.iologic.apps.dtn;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Abhishanth Padarthy on 30-01-2018.
 */

public class Bandwidth extends Thread {

    private final BluetoothSocket bandwidthSocket;
    private final InputStream bandwidthInStream;
    private final OutputStream bandwidthOutStream;
    private byte[] bandwidthBuffer; // bandwidthBuffer store BW bytes for the stream

    private Handler bandwidthHandler;

    public Bandwidth(BluetoothSocket socket,Handler handler) {
        bandwidthSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error occurred when creating output stream", e);
        }

        bandwidthInStream = tmpIn;
        bandwidthOutStream = tmpOut;

        bandwidthHandler = handler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                bandwidthBuffer = new byte[1024];
                int numBytes; // bytes returned from read()

                // Log.i(Constants.TAG, "Bandwidth Check: " + bandwidthCheck);

                if (bandwidthInStream.available() > 0) {
                    // Read from the InputStream.
                    numBytes = bandwidthInStream.read(bandwidthBuffer);
                    // Send the obtained bytes to the UI activity.
                    //  Log.i(Constants.TAG, "Number Of Speed Bytes Received: " + numBytes);
                        Message readMsg = bandwidthHandler.obtainMessage(
                                Constants.MessageConstants.BW_READ, numBytes, -1,
                                bandwidthBuffer);
                        readMsg.sendToTarget();
                    }else {
                    SystemClock.sleep(100);
                }
            } catch (IOException e) {
                Log.d(Constants.TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    public void write(byte[] bytes) {
        try {

            bandwidthBuffer = bytes;

            String testMessage = new String(bandwidthBuffer);
            Log.i(Constants.TAG, "BW Sending: " + testMessage);

            bandwidthOutStream.write(bandwidthBuffer);

            // Share the sent message with the UI activity.
            Message writtenMsg = bandwidthHandler.obtainMessage(
                    Constants.MessageConstants.BW_WRITE, -1, -1, bandwidthBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error occurred when sending BW", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    bandwidthHandler.obtainMessage(Constants.MessageConstants.BW_FAIL_TO_SEND);
            Bundle bundle = new Bundle();
            bundle.putString("status",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            bandwidthHandler.sendMessage(writeErrorMsg);
        }
    }

    public void flushOutStream() {
        try {
            bandwidthOutStream.flush();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not flush out BW stream", e);
        }
    }

    public void cancel() {
        try {
            bandwidthSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}
