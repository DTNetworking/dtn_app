package me.iologic.apps.dtn;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.TimeUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.time.Duration;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Abhishanth Padarthy on 30-01-2018.
 */

public class BandwidthBytesT extends Thread {

    private final BluetoothSocket bandwidthSocket;
    private final InputStream bandwidthInStream;
    private final OutputStream bandwidthOutStream;
    DataInputStream dIn;
    DataOutputStream dOut; // Go Dynamic!!!
    Timer t;
    int counter;
    int sizeOfDataBeingSent;
    int previousSize;
    int amountOfDataSent;
    int written;

    boolean isFirstTime;

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
        isFirstTime = true;
        counter = 1;

        t = new Timer();
        // bandwidthBuffer = new byte[1024];
        written = 0;
    }

    @Override
    public void run() {
        while (true) {
            dIn = new DataInputStream(bandwidthInStream);
            try {
                int length = dIn.readInt();                    // read length of incoming message
                if (length > 0) {
                    byte[] message = new byte[length];
                    dIn.readFully(message, 0, message.length); // read the message
                    Log.i(Constants.TAG, "BW Data I am receiving: " + message.length);
                    // Send the obtained bytes to the UI activity.
                    //   Log.i(Constants.TAG, "Number Of LightningMcQueen Bytes Received: " + numBytes);
                    Message readMsg = bandwidthHandler.obtainMessage(
                            Constants.MessageConstants.BW_READ, length, -1,
                            message);
                    readMsg.sendToTarget();
                }
            } catch (IOException e) {
                Log.d(Constants.TAG, "Input stream was disconnected", e);
                break;
            }
        }

    }

    public void write(byte[] bytes) {
        try {
            dOut = new DataOutputStream(bandwidthOutStream);
            if (isFirstTime) {
                isFirstTime = false;
                // Share the sent message with the UI activity.
                Message writtenBWStatus = bandwidthHandler.obtainMessage(
                        Constants.MessageConstants.BW_START_WRITE, counter, -1, bytes);
                writtenBWStatus.sendToTarget();


            }

            sendingStartTime = System.nanoTime();

            dOut.writeInt(bytes.length); // write length of the message
            dOut.write(bytes);// write the message
            flushOutStream();


            sendingEndTime = System.nanoTime();
            duration = sendingEndTime - sendingStartTime;

            // Log.i(Constants.TAG, "Duration it took to send 64 KB file: " + duration);

            Message writtenMsg = bandwidthHandler.obtainMessage(
                    Constants.MessageConstants.BW_WRITE, counter, -1, null);
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

    public void writeAsByte(int data) {
        try {
            dOut = new DataOutputStream(bandwidthOutStream);
            if (isFirstTime) {
                isFirstTime = false;
                // Share the sent message with the UI activity.
                Message writtenBWStatus = bandwidthHandler.obtainMessage(
                        Constants.MessageConstants.BW_START_WRITE, counter, -1, data);
                writtenBWStatus.sendToTarget();

                // Share the sent message with the UI activity.
                Thread sendMessage = new Thread() {
                    @Override
                    public void run() {
                        t.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if ((sizeOfDataBeingSent != 0) & (sizeOfDataBeingSent <= Constants.Packet.BW_FILE_SIZE)) {
                                    //  Log.i(Constants.TAG, "I am repeating BW_WRITE " + sizeOfDataBeingSent + " " + previousSize);
                                    amountOfDataSent = sizeOfDataBeingSent - previousSize;
                                    previousSize = sizeOfDataBeingSent;
                                    //  Log.i(Constants.TAG, "Data now sent:" + amountOfDataSent);
                                    Message writtenMsg = bandwidthHandler.obtainMessage(
                                            Constants.MessageConstants.BW_WRITE, counter, -1, null);
                                    writtenMsg.sendToTarget();
                                } else {
                                    t.cancel();
                                    return;
                                }
                            }
                        }, 0, Constants.Miscellaneous.BW_TIME_INTERVAL);
                    }
                };

                if (!(sendMessage.isAlive())) {
                    sendMessage.start();
                }

            }

            //  sendingStartTime = System.nanoTime();

            dOut.writeInt(1); // write length of the message
            dOut.writeByte(data);// write the message
            written++;
            sizeOfDataBeingSent = written;
            //Log.i(Constants.TAG, "BW Data I am sending: " + written);
            flushOutStream();


//            sendingEndTime = System.nanoTime();
//            duration = sendingEndTime - sendingStartTime;

            // Log.i(Constants.TAG, "Duration it took to send 1MB file: " + duration);
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

    public int getBWWriteSize() {
        //Log.i(Constants.TAG, "AmountOfDataBeingSent: " + amountOfDataSent);
        return amountOfDataSent;
    }


    public void flushOutStream() {
        try {
            dOut.flush();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not flush out BW stream", e);
        }
    }

    public void checkBandwidth(FileServices fileService, File tempFileRead) {
        byte[] getData = fileService.readTempFile(tempFileRead);
        // Log.i(Constants.TAG, "checkBandwidth() getData Size: " + getData.length);

//        byte[] sendData; // Breaking 1 MB file into 64 KB packets.
//        int startPacketIndex = 0;
//        while (counter != (Constants.Packet.BW_COUNTER + 1)) {
//            Message readMsg = bandwidthHandler.obtainMessage(
//                    Constants.MessageConstants.BW_PACKET_LOSS_CHECK, counter, -1,
//                    null);
//            readMsg.sendToTarget();
//
//            sendData = Arrays.copyOfRange(getData, startPacketIndex, (startPacketIndex + Constants.Packet.BW_PACKET_SIZE) - 1);
//            write(sendData);
//            counter++;
//            startPacketIndex += Constants.Packet.BW_PACKET_SIZE;
//            // Log.i(Constants.TAG, "BW Counter: " + counter + " Packet Index:" + startPacketIndex + " sendData size: " + sendData.length);
//        }
//        if (counter == (Constants.Packet.BW_COUNTER + 1)) {
//            counter = 1; // Reset Counter to 1
//        }
//        byte[] data = new byte[(int) tempFileRead.length()];
//        try {
//            FileInputStream fin = new FileInputStream(tempFileRead);
//            int n = 0;
//            while ((n = fin.read(data, n, data.length - n)) > 0) {
//                writeAsByte(n);
//            }
//
//        } catch (FileNotFoundException e) {
//            Log.e(Constants.TAG, "File not found (from read() file): " + e.toString());
//        } catch (IOException e) {
//            Log.e(Constants.TAG, "Can not read file: " + e.toString());
//        }


//        ByteArrayInputStream bis = new ByteArrayInputStream(getData);
//
//        int c, written = 0;
//        while ((c = bis.read()) != -1) {
//            written++;
//           // Log.i(Constants.TAG, "ByteArrayIS: " + written);
//            writeAsByte(c);
//        }

        for (int i = 0; i < 10; i++) {
            write(getData);
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

    public void cancel() {
        try {
            bandwidthSocket.close();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}