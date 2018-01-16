package me.iologic.apps.dtn;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by vinee on 15-01-2018.
 *
 * Documentation:
 *
 * No Of Packets To Be Sent =(bytes.length/PACKET_SIZE).
 *
 */

class BluetoothBytesT extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream

    long sendingStartTime, sendingEndTime, duration;

    private Handler mHandler;

    private static final int PACKET_SIZE = 2; // 2 Bytes Per Packet.
    private static final int NO_OF_PACKETS = 25;


    public BluetoothBytesT(BluetoothSocket socket, Handler handler){
        mmSocket = socket;
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

        mmInStream = tmpIn;
        mmOutStream = tmpOut;

            mHandler = handler;
    }

    public void run() { // For Reading Messages
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
                // Send the obtained bytes to the UI activity.
                Message readMsg = mHandler.obtainMessage(
                        Constants.MessageConstants.MESSAGE_READ, numBytes, -1,
                        mmBuffer);
                readMsg.sendToTarget();
            } catch (IOException e) {
                Log.d(Constants.TAG, "Input stream was disconnected", e);
                break;
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {

            mmBuffer = bytes;

            sendingStartTime = System.nanoTime();
                mmOutStream.write(mmBuffer);
            sendingEndTime = System.nanoTime();

            duration = sendingEndTime - sendingStartTime;

            Log.i(Constants.TAG, "Time Calculated:" + sendingEndTime + "And " + sendingStartTime + "And " + duration);


            // Share the sent message with the UI activity.
            Message writtenMsg = mHandler.obtainMessage(
                    Constants.MessageConstants.MESSAGE_WRITE, -1, (int)(duration/1000000), mmBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Error occurred when sending data", e);

            // Send a failure message back to the activity.
            Message writeErrorMsg =
                    mHandler.obtainMessage(Constants.MessageConstants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString("status",
                    "Couldn't send data to the other device");
            writeErrorMsg.setData(bundle);
            mHandler.sendMessage(writeErrorMsg);
        }
    }

    public void flushOutStream(){
        try {
            mmOutStream.flush();
        } catch (IOException e){
            Log.e(Constants.TAG, "Could not flush out stream", e);
        }
    }

    public void checkBandwidth(FileServices fileService){
        String fileData = fileService.readTempFile(Constants.testFileName);
        this.write(fileData.getBytes());
    }

    public long getTotalBandwidthDuration(){
        return (TimeUnit.NANOSECONDS.toSeconds(duration));
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}

