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
 * Created by vinee on 15-01-2018.
 * <p>
 * Documentation:
 * <p>
 * No Of Packets To Be Sent =(bytes.length/PACKET_SIZE).
 */

class BluetoothBytesT extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private int GlobalNumBytesRead;

    long sendingStartTime, sendingEndTime, duration, ACKStartTime;

    private Handler mHandler;

    long writingStartTime, readingEndTime, packetDuration;

    StopWatch stopW;

    public BluetoothBytesT(BluetoothSocket socket, Handler handler, StopWatch stpW) {
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
        stopW = stpW;
    }


    public void run() {
        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()

                // Log.i(Constants.TAG, "BandwidthBytesT Check: " + bandwidthCheck);

                if (mmInStream.available() > 0) {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    GlobalNumBytesRead = numBytes;
                    Log.i(Constants.TAG, "Number Of Message Bytes Received: " + numBytes);
                    Message readMsg = mHandler.obtainMessage(
                            Constants.MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
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

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {

            mmBuffer = bytes;

            String testMessage = new String(mmBuffer);
            Log.i(Constants.TAG, "Message Sending: " + testMessage);

            sendingStartTime = System.nanoTime();
            mmOutStream.write(mmBuffer);
            flushOutStream();
            sendingEndTime = System.nanoTime();

            duration = sendingEndTime - sendingStartTime;

            Log.i(Constants.TAG, "Time Calculated:" + sendingEndTime + " And " + sendingStartTime + " And " + duration);


            // Share the sent message with the UI activity.
            Message writtenMsg = mHandler.obtainMessage(
                    Constants.MessageConstants.MESSAGE_WRITE, -1, (int) (duration), mmBuffer);
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

    public void writePackets(byte[] bytes) {
        int i = 0;
        int initi = 0;
        int j = 0;
        int m = 0;
        int offset = 0;
        byte[] packet = new byte[2];
        String MessagePacket;

        Log.i(Constants.TAG, "Bytes length from writePackets(): " + bytes.length);

        stopW.start();

        for (j = initi; j < (Constants.Packet.PACKET_SIZE + offset); j++) {
            try {
                if (j == bytes.length) {
                    try {
                        MessagePacket = new String(packet); // Treating 2 bytes as a single data packet
                        mmOutStream.write(MessagePacket.getBytes());
                    } catch (IOException WriteE) {
                        Log.i(Constants.TAG, "Write Error: " + WriteE);
                    }
                    break;
                }
                packet[m] = bytes[j];
                Log.i(Constants.TAG, "Byte Reading from writePackets(): " + new String(packet));
                i++;
                m++;
                if ((i % 2) == 0 && (i != 0)) {
                    initi = i;
                    offset = offset + 2;
                    m = 0;

                    MessagePacket = new String(packet); // Treating 2 bytes as a single data packet
                    mmOutStream.write(MessagePacket.getBytes());
                    packet = new byte[2]; // Erase old Data
                }

                Message readMsg = mHandler.obtainMessage(
                        Constants.MessageConstants.MESSAGE_WRITE, -1, -1,
                        mmBuffer);
                readMsg.sendToTarget();

            } catch (IOException WriteE) {
                Log.i(Constants.TAG, "Write Error: " + WriteE);
            }
        }
    }

    public double getPacketLoss(){
        double packetLost = ((Constants.Packet.MSG_PACKET_SIZE - GlobalNumBytesRead) / (Constants.Packet.MSG_PACKET_SIZE)) * 100;
        Log.i(Constants.TAG, "Packet Lost Msg: " + (Constants.Packet.MSG_PACKET_SIZE - GlobalNumBytesRead) / (Constants.Packet.MSG_PACKET_SIZE));
        return packetLost;
    }


    public void flushOutStream() {
        try {
            mmOutStream.flush();
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not flush out stream", e);
        }
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

