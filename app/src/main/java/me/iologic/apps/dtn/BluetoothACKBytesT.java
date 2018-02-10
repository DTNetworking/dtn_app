package me.iologic.apps.dtn;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by vinee on 19-01-2018.
 */

public class BluetoothACKBytesT extends Thread {

    private final BluetoothSocket mmACKSocket;
    private final InputStream mmACKInStream;
    private final OutputStream mmACKOutStream;
    private byte[] mmACKBuffer; // mmBuffer store ACK bytes for the stream

    private Handler mACKHandler;

    public BluetoothACKBytesT(BluetoothSocket socket, Handler handler) {
        mmACKSocket = socket;
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

        mmACKInStream = tmpIn;
        mmACKOutStream = tmpOut;

        mACKHandler = handler;
    }

    @Override
    public void run() {
        while (true) {
            try {
                mmACKBuffer = new byte[]{' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
                int numBytes; // bytes returned from read()

                if (mmACKInStream.available() > 0) {
                    // Read from the InputStream.
                    numBytes = mmACKInStream.read(mmACKBuffer);
                    // Send the obtained bytes to the UI activity.
                    Log.i(Constants.TAG, "Number Of Speed Bytes Received (ACK read()): " + numBytes);
                    Message readMsg = mACKHandler.obtainMessage(
                            Constants.MessageConstants.ACK_READ, numBytes, -1,
                            mmACKBuffer);
                    readMsg.sendToTarget();
                } else {

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

            mmACKBuffer = bytes;

            String testMessage = new String(mmACKBuffer);
            Log.i(Constants.TAG, "ACK Sending: " + testMessage);

            mmACKOutStream.write(mmACKBuffer);
            flushOutStream();
            // Share the sent message with the UI activity.
            Message writtenMsg = mACKHandler.obtainMessage(
                    Constants.MessageConstants.ACK_WRITE, -1, -1, mmACKBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error occurred when sending ACK", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    mACKHandler.obtainMessage(Constants.MessageConstants.ACK_FAIL_TO_SEND);
            Bundle bundle = new Bundle();
            bundle.putString("status",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            mACKHandler.sendMessage(writeErrorMsg);
        }
    }

    public void flushOutStream() {
        try {
            mmACKOutStream.flush();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not flush out ACK stream", e);
        }
    }

    public void cancel() {
        try {
            mmACKSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }

}

