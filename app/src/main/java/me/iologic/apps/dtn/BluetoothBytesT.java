package me.iologic.apps.dtn;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    DataInputStream dIn;
    DataOutputStream dOut; // Go Dynamic!!!

    long sendingStartTime, sendingEndTime, duration;

    private Handler mHandler;

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
            Log.i(Constants.TAG, "Reading Data");

            dIn = new DataInputStream(mmInStream);
            try {
                int length = dIn.readInt();                    // read length of incoming message
                if (length > 0) {
                    byte[] message = new byte[length];
                    dIn.readFully(message, 0, message.length); // read the message
                    Log.i(Constants.TAG, "Length Of Message: " + message.length + " bytes");
                    Message readMsg = mHandler.obtainMessage(
                            Constants.MessageConstants.MESSAGE_READ, length, -1,
                            message);
                    readMsg.sendToTarget();
                }
            } catch (IOException e) {
                Log.e(Constants.testFileName, e.toString());
            }
        }
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        stopW.start();

        dOut = new DataOutputStream(mmOutStream);

        Log.e(Constants.TAG, "Started Writing to Socket. Go Dynamic!!!");

        try {
            sendingStartTime = System.nanoTime();

            dOut.writeInt(bytes.length); // write length of the message
            dOut.write(bytes);           // write the message

            flushOutStream();
            sendingEndTime = System.nanoTime();
            duration = sendingEndTime - sendingStartTime;

            // Share the sent message with the UI activity.
            Message writtenMsg = mHandler.obtainMessage(
                    Constants.MessageConstants.MESSAGE_WRITE, -1, (int) (duration), bytes);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e(Constants.TAG, e.toString());
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

//    public void writePackets(byte[] bytes) {
//        int i = 0;
//        int initi = 0;
//        int j = 0;
//        int m = 0;
//        int offset = 0;
//        byte[] packet = new byte[2];
//        String MessagePacket;
//
//        Log.i(Constants.TAG, "Bytes length from writePackets(): " + bytes.length);
//
//        stopW.start();
//
//        for (j = initi; j < (Constants.Packet.PACKET_SIZE + offset); j++) {
//            try {
//                if (j == bytes.length) {
//                    try {
//                        MessagePacket = new String(packet); // Treating 2 bytes as a single data packet
//                        mmOutStream.write(MessagePacket.getBytes());
//                    } catch (IOException WriteE) {
//                        Log.i(Constants.TAG, "Write Error: " + WriteE);
//                    }
//                    break;
//                }
//                packet[m] = bytes[j];
//                Log.i(Constants.TAG, "Byte Reading from writePackets(): " + new String(packet));
//                i++;
//                m++;
//                if ((i % 2) == 0 && (i != 0)) {
//                    initi = i;
//                    offset = offset + 2;
//                    m = 0;
//
//                    MessagePacket = new String(packet); // Treating 2 bytes as a single data packet
//                    mmOutStream.write(MessagePacket.getBytes());
//                    packet = new byte[2]; // Erase old Data
//                }
//
//                Message readMsg = mHandler.obtainMessage(
//                        Constants.MessageConstants.MESSAGE_WRITE, -1, -1,
//                        mmBuffer);
//                readMsg.sendToTarget();
//
//            } catch (IOException WriteE) {
//                Log.i(Constants.TAG, "Write Error: " + WriteE);
//            }
//        }
//    }

    public double getPacketLoss(int EditWritten, String receivedNumBytes) {
        if (receivedNumBytes.length() > 1) {
            try {
                int receivedNumBytesInt = Integer.parseInt(receivedNumBytes.trim());
                double packetLost = ((double) (EditWritten - receivedNumBytesInt) / (double) EditWritten) * 100;
                Log.i(Constants.TAG, "Packet Lost Msg: " + EditWritten + " " + Integer.valueOf(receivedNumBytesInt));
                return packetLost;
            } catch (NumberFormatException e) {
                Log.i(Constants.TAG, "No bytes received.");
            }
        } else {

            double packetLost = ((double) (EditWritten - Integer.valueOf(receivedNumBytes)) / (double) EditWritten) * 100;
            Log.i(Constants.TAG, "Packet Lost Msg: " + EditWritten + " " + Integer.valueOf(receivedNumBytes));
            return packetLost;
        }
        return -1;
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

