package me.iologic.apps.dtn;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by Abhishanth Padarthy on 30-01-2018.
 */

public class BandwidthBytesT extends Thread {

    private final BluetoothSocket bandwidthSocket;
    private final InputStream bandwidthInStream;
    private final OutputStream bandwidthOutStream;
    private byte[] bandwidthBuffer; // bandwidthBuffer store BW bytes for the stream
    private int GlobalNumBytesRead;
    int counter;

    long sendingStartTime, sendingEndTime, duration;

    private Handler bandwidthHandler;

    public BandwidthBytesT(BluetoothSocket socket, Handler handler) {
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
        GlobalNumBytesRead = 0;
        counter = 1;
        // bandwidthBuffer = new byte[1024];
    }

    @Override
    public void run() {
        while (true) {
            try {
                bandwidthBuffer = new byte[Constants.Packet.BW_FILE_SIZE];
                int numBytes; // bytes returned from read()
                // Log.i(Constants.TAG, "BandwidthBytesT Check: " + bandwidthCheck);

                if (bandwidthInStream.available() > 0) {
                    // Read from the InputStream.
                    numBytes = bandwidthInStream.read(bandwidthBuffer);
                    GlobalNumBytesRead += numBytes;
                    // Send the obtained bytes to the UI activity.
                    //  Log.i(Constants.TAG, "Number Of Speed Bytes Received: " + numBytes);
                } else {
                    SystemClock.sleep(100);
                }
            } catch (IOException e) {
                Log.d(Constants.TAG, "Input stream was disconnected", e);
                break;
            }

            Log.i(Constants.TAG, "GlobalBWNumBytesRead: " + GlobalNumBytesRead);

            if (GlobalNumBytesRead >= Constants.Packet.BW_FILE_SIZE) {
                Message readMsg = bandwidthHandler.obtainMessage(
                        Constants.MessageConstants.BW_READ, GlobalNumBytesRead, -1,
                        bandwidthBuffer);
                readMsg.sendToTarget();
                GlobalNumBytesRead = 0;
            }
        }
    }

    public void write(byte[] bytes) {
        try {

            bandwidthBuffer = bytes;
            //   Log.i(Constants.TAG, "bandwidthBuffer size(): " + bandwidthBuffer.length);
            String testMessage = new String(bandwidthBuffer);
            //  Log.i(Constants.TAG, "BW Sending: " + testMessage);

            // Share the sent message with the UI activity.
            Message writtenBWStatus = bandwidthHandler.obtainMessage(
                    Constants.MessageConstants.BW_START_WRITE, counter, -1, bandwidthBuffer);
            writtenBWStatus.sendToTarget();

            sendingStartTime = System.nanoTime();
            bandwidthOutStream.write(bandwidthBuffer);
            flushOutStream();
            sendingEndTime = System.nanoTime();
            duration = sendingEndTime - sendingStartTime;


            // Share the sent message with the UI activity.
            Message writtenMsg = bandwidthHandler.obtainMessage(
                    Constants.MessageConstants.BW_WRITE, counter, -1, bandwidthBuffer);
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

    public void checkBandwidthAsAFile(FileServices fileService, File tempFileRead) {
        byte[] getData = fileService.readTempFile(tempFileRead);
        Log.i(Constants.TAG, "checkBandwidth() getData Size: " + getData.length);
        write(getData);
    }

    public void checkBandwidth(FileServices fileService, File tempFileRead) {
        byte[] getData = fileService.readTempFile(tempFileRead);
        // Log.i(Constants.TAG, "checkBandwidth() getData Size: " + getData.length);

        byte[] sendData; // Breaking 1 MB file into 64 KB packets. So total 16 packets.
        int startPacketIndex = 0;
        while (counter != (Constants.Packet.BW_COUNTER + 1)) { // 16 Packets
            sendData = Arrays.copyOfRange(getData, startPacketIndex, (startPacketIndex + Constants.Packet.BW_PACKET_SIZE) - 1);
            write(sendData);
            counter++;
            startPacketIndex += Constants.Packet.BW_PACKET_SIZE;
            // Log.i(Constants.TAG, "BW Counter: " + counter + " Packet Index:" + startPacketIndex + " sendData size: " + sendData.length);
        }
        if (counter == (Constants.Packet.BW_COUNTER + 1)) {
            counter = 1; // Reset Counter to 1
        }
    }

    public double getTotalBandwidthDuration() {
        //  Log.i(Constants.TAG, "Duration:" + duration);
        //  Log.i(Constants.TAG, "Duration in seconds: " + TimeUnit.NANOSECONDS.toSeconds(duration));
      /*  if (TimeUnit.NANOSECONDS.toSeconds(duration) == 0) {
            duration = 1;
            Log.i(Constants.TAG, "Sending duration as: " + duration);
            return duration;
        } */
        return ((double) duration / 1000000000.0);
    }

    public double getPacketLoss(byte[] ReceivedBWData) {
        double packetLost = ((double)(Constants.Packet.BW_FILE_SIZE - ReceivedBWData.length) / (double)(Constants.Packet.BW_FILE_SIZE)) * 100;
        Log.i(Constants.TAG, "Packet Lost BW: " + packetLost);
        return packetLost;
    }

    public void cancel() {
        try {
            bandwidthSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}
