package me.iologic.apps.dtn;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Abhishanth Padarthy on 30-01-2018.
 */

public class BandwidthBytesT extends Thread {

    private final BluetoothSocket bandwidthSocket;
    private final InputStream bandwidthInStream;
    private final OutputStream bandwidthOutStream;
    private byte[] bandwidthBuffer; // bandwidthBuffer store BW bytes for the stream
    private TextView checkBandwidthText;

    long sendingStartTime, sendingEndTime, duration;

    private Handler bandwidthHandler;

    public BandwidthBytesT(BluetoothSocket socket, Handler handler, TextView checkBandwidthTxt) {
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
        checkBandwidthText = checkBandwidthTxt;
        // bandwidthBuffer = new byte[1024];
    }

    @Override
    public void run() {
        while (true) {
            try {
                bandwidthBuffer = new byte[1024 * 1024];
                int numBytes; // bytes returned from read()

                // Log.i(Constants.TAG, "BandwidthBytesT Check: " + bandwidthCheck);

                if (bandwidthInStream.available() > 0) {
                    // Read from the InputStream.
                    numBytes = bandwidthInStream.read(bandwidthBuffer);
                    // Send the obtained bytes to the UI activity.
                    //  Log.i(Constants.TAG, "Number Of Speed Bytes Received: " + numBytes);
                    Message readMsg = bandwidthHandler.obtainMessage(
                            Constants.MessageConstants.BW_READ, numBytes, -1,
                            bandwidthBuffer);
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

            bandwidthBuffer = bytes;
            Log.i(Constants.TAG, "bandwidthBuffer size(): " + bandwidthBuffer.length);
            String testMessage = new String(bandwidthBuffer);
            //  Log.i(Constants.TAG, "BW Sending: " + testMessage);
            // Share the sent message with the UI activity.
            Message writtenBWStatus = bandwidthHandler.obtainMessage(
                    Constants.MessageConstants.BW_START_WRITE, -1, -1, bandwidthBuffer);
            writtenBWStatus.sendToTarget();

            sendingStartTime = System.nanoTime();
            bandwidthOutStream.write(bandwidthBuffer);
            sendingEndTime = System.nanoTime();
            duration = sendingEndTime - sendingStartTime;


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

    public void checkBandwidth(FileServices fileService, File tempFileRead) {
        byte[] getData = fileService.readTempFile(tempFileRead);
        Log.i(Constants.TAG, "checkBandwidth() getData Size: " + getData.length);

        byte[] sendData; // Breaking 1 MB file into 64 KB packets. So total 16 packets.
        int counter = 1;
        int startPacketIndex = 0;
        while (counter != 17) { // (1024 * 1024) / (1024 * 64) = 16 Packets
            sendData = Arrays.copyOfRange(getData, startPacketIndex, (startPacketIndex + Constants.Packet.BW_PACKET_SIZE) - 1);
            write(sendData);
            counter++;
            startPacketIndex += Constants.Packet.BW_PACKET_SIZE;
        }
    }

    public long getTotalBandwidthDuration() {
        Log.i(Constants.TAG, "Duration:" + duration);
        Log.i(Constants.TAG, "Duration in seconds: " + TimeUnit.NANOSECONDS.toSeconds(duration));
        if (TimeUnit.NANOSECONDS.toSeconds(duration) == 0) {
            duration = 1;
            Log.i(Constants.TAG, "Sending duration as: " + duration);
            return duration;
        }
        return (TimeUnit.NANOSECONDS.toSeconds(duration));
    }

    public void cancel() {
        try {
            bandwidthSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}
