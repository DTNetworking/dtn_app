package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by vinee on 15-01-2018.
 */

class BluetoothConnectClientT extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothSocket mmACKClientSocket;
    private final BluetoothSocket mmBWClientSocket;

    //second connection
    private final BluetoothSocket secondMMSocket;
    private final BluetoothSocket secondMMACKClientSocket;
    private final BluetoothSocket secondMMBWClientSocket;

    private final BluetoothDevice mmDevice;
    BluetoothAdapter mBluetoothAdapter;

    public static final String TAG = "DTNLogs";
    Handler btConnectionStatus;
    Message btConnectionStatusMsg;
    Message btConnectionACKStatusMsg;
    Message btConnectionBWStatusMsg;

    //for 2nd connection
    Handler secondBtConnectionStatus;
    Message secondBtConnectionStatusMsg;
    Message secondBtConnectionACKStatusMsg;
    Message secondBtConnectionBWStatusMsg;

    long pairingStartTime, pairingEndTime, duration, secondPairingStartTime, secondPairingEndTime, secondDuration;
    int retry;

    private static final UUID MY_UUID = UUID.fromString("6e7bd336-5676-407e-a41c-0691e1964345"); // UUID is uniquely generated
    private static final UUID ACK_UUID = UUID.fromString("b03901e4-710c-4509-9718-a3d15882d050"); // UUID is uniquely generated
    private static final UUID BW_UUID = UUID.fromString("aa401ee7-3bb2-410c-9dda-2128726513a1"); // UUID is uniquely generated

    public BluetoothConnectClientT(BluetoothDevice device, BluetoothAdapter getBluetoothAdapter, Handler getBtConnectionStatus) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        BluetoothSocket ACKtmp = null;
        BluetoothSocket BWtmp = null;

        //temporary object for second connection
        BluetoothSocket secondTmp = null;
        BluetoothSocket secondACKTmp = null;
        BluetoothSocket secondBWTmp = null;

        mmDevice = device;

        mBluetoothAdapter = getBluetoothAdapter;
        btConnectionStatus = getBtConnectionStatus;
        //secondBtConnectionStatus = getBtConnectionStatus;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice. Allowing Insecure connections to avoid Pairing Key.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            ACKtmp = device.createInsecureRfcommSocketToServiceRecord(ACK_UUID);
            BWtmp = device.createInsecureRfcommSocketToServiceRecord(BW_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }

        //for 2nd connection
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice. Allowing Insecure connections to avoid Pairing Key.
            // MY_UUID is the app's UUID string, also used in the server code.
            secondTmp = device.createInsecureRfcommSocketToServiceRecord(MY_SECOND_UUID);
            secondACKTmp = device.createInsecureRfcommSocketToServiceRecord(SECOND_ACK_UUID);
            secondBWTmp = device.createInsecureRfcommSocketToServiceRecord(SECOND_BW_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Second Socket's create() method failed", e);
        }

        mmSocket = tmp;
        mmACKClientSocket = ACKtmp;
        mmBWClientSocket = BWtmp;

        //second connection
        secondMMSocket = secondTmp;
        secondMMACKClientSocket = secondACKTmp;
        secondMMBWClientSocket = secondBWTmp;

        btConnectionACKStatusMsg = Message.obtain();
        btConnectionBWStatusMsg = Message.obtain();

        //for 2nd connection
        secondBtConnectionACKStatusMsg = Message.obtain();
        secondBtConnectionBWStatusMsg = Message.obtain();

        retry = 0;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        btConnectionStatusMsg = Message.obtain();
        secondBtConnectionStatusMsg = Message.obtain();

        if (retry != 0) {
            Log.i(Constants.TAG, "I am re-trying to connect to the DTN device. " + android.os.Process.myTid() + " Retry: " + retry);
        } else { // Log.i(Constants.TAG, "I am connecting to the DTN device for the first time");
        }

        // BW Part
  /*      try {
            mmBWClientSocket.connect();
        } catch (IOException e) {
            btConnectionBWStatusMsg.arg1 = -2;
            btConnectionStatus.sendMessage(btConnectionBWStatusMsg);
            Log.e(Constants.TAG, "I could not connect to BW Socket on the server side");
            try {
                mmBWClientSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);

            }

            return;
        }

        // BW Part for 2nd connection
        try {
            secondMMBWClientSocket.connect();
        } catch (IOException e) {
            secondBtConnectionBWStatusMsg.arg1 = -2;
            secondBtConnectionStatus.sendMessage(secondBtConnectionBWStatusMsg);
            Log.e(Constants.TAG, "I could not connect to BW Socket on the server side");
            try {
                secondMMBWClientSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);

            }

            return;
        }

        btConnectionBWStatusMsg.arg1 = 100;
        btConnectionStatus.sendMessage(btConnectionBWStatusMsg);

        secondBtConnectionBWStatusMsg.arg1 = 101;
        secondBtConnectionStatus.sendMessage(secondBtConnectionBWStatusMsg); */

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            pairingStartTime = System.nanoTime();
            mmSocket.connect();
            pairingEndTime = System.nanoTime();
            duration = (pairingEndTime - pairingStartTime);
        } catch (IOException connectException) {
            btConnectionStatusMsg.arg1 = -1;
            btConnectionStatus.sendMessage(btConnectionStatusMsg);
            //  Log.i(Constants.TAG, "Connect Exception:" + connectException);

            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }

            return;
        }

        //for 2nd connection
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            secondPairingStartTime = System.nanoTime();
            secondMMSocket.connect();
            secondPairingEndTime = System.nanoTime();
            secondDuration = (secondPairingEndTime - secondPairingStartTime);
        } catch (IOException connectException) {
            secondBtConnectionStatusMsg.arg1 = -1;
            secondBtConnectionStatus.sendMessage(secondBtConnectionStatusMsg);
            //  Log.i(Constants.TAG, "Connect Exception:" + connectException);

            // Unable to connect; close the socket and return.
            try {
                secondMMSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }

            return;
        }

        retry++;

        btConnectionStatusMsg.arg1 = 1;
        btConnectionStatusMsg.arg2 = (int) (duration / 1000000);

        btConnectionStatus.sendMessage(btConnectionStatusMsg);

        //for 2nd connection
        secondBtConnectionStatusMsg.arg1 = 8;
        secondBtConnectionStatusMsg.arg2 = (int) (secondDuration / 1000000);

        secondBtConnectionStatus.sendMessage(secondBtConnectionStatusMsg);


        // ACK Part
     /*   try {
            mmACKClientSocket.connect();
        } catch (IOException e) {
            btConnectionACKStatusMsg.arg1 = -2;
            btConnectionStatus.sendMessage(btConnectionACKStatusMsg);
            Log.e(Constants.TAG, "I could not connect to ACK Socket on the server side");
            try {
                mmACKClientSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);

            }

            return;
        }

        //ACK part for 2nd connection
        try {
            secondMMACKClientSocket.connect();
        } catch (IOException e) {
            secondBtConnectionACKStatusMsg.arg1 = -2;
            secondBtConnectionStatus.sendMessage(secondBtConnectionACKStatusMsg);
            Log.e(Constants.TAG, "I could not connect to ACK Socket on the server side");
            try {
                secondMMACKClientSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);

            }

            return;
        }

        btConnectionACKStatusMsg.arg1 = 2;
        btConnectionStatus.sendMessage(btConnectionACKStatusMsg);

        secondBtConnectionACKStatusMsg.arg1 = 9;
        secondBtConnectionStatus.sendMessage(secondBtConnectionACKStatusMsg); */
    }

    public BluetoothSocket getClientSocket() {
        return mmSocket;
    }

    public BluetoothSocket getACKClientSocket() {
        return mmACKClientSocket;
    }

    public BluetoothSocket getBWClientSocket() {
        return mmBWClientSocket;
    }

    //for 2nd connection
    public BluetoothSocket getsecondClientSocket() {
        return secondMMSocket;
    }

    public BluetoothSocket getSecondACKClientSocket() {
        return secondMMACKClientSocket;
    }

    public BluetoothSocket getSecondBWClientSocket() {
        return secondMMBWClientSocket;
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
            mmACKClientSocket.close();
            mmBWClientSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }

        //for 2nd connection
        try {
            secondMMSocket.close();
            secondMMACKClientSocket.close();
            secondMMBWClientSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}

