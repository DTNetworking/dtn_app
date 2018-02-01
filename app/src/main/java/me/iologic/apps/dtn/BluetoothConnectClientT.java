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
    private final BluetoothDevice mmDevice;
    BluetoothAdapter mBluetoothAdapter;

    public static final String TAG = "DTNLogs";
    Handler btConnectionStatus;
    Message btConnectionStatusMsg;
    Message btConnectionACKStatusMsg;
    Message btConnectionBWStatusMsg;

    long pairingStartTime, pairingEndTime, duration;
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
        mmDevice = device;

        mBluetoothAdapter = getBluetoothAdapter;
        btConnectionStatus = getBtConnectionStatus;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            ACKtmp = device.createRfcommSocketToServiceRecord(ACK_UUID);
            BWtmp = device.createRfcommSocketToServiceRecord(BW_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
        mmACKClientSocket = ACKtmp;
        mmBWClientSocket = BWtmp;

        btConnectionACKStatusMsg = Message.obtain();
        btConnectionBWStatusMsg = Message.obtain();

        retry = 0;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        btConnectionStatusMsg = Message.obtain();

        if (retry != 0) {
            Log.i(Constants.TAG, "I am re-trying to connect to the DTN device. " + android.os.Process.myTid() + " Retry: " + retry);
        } else { // Log.i(Constants.TAG, "I am connecting to the DTN device for the first time");
        }

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

        retry++;

        btConnectionStatusMsg.arg1 = 1;
        btConnectionStatusMsg.arg2 = (int) (duration / 1000000);

        btConnectionStatus.sendMessage(btConnectionStatusMsg);

        // ACK Part
        try {
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

        btConnectionACKStatusMsg.arg1 = 2;
        btConnectionStatus.sendMessage(btConnectionACKStatusMsg);

        // BW Part
        try {
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

        btConnectionBWStatusMsg.arg1 = 100;
        btConnectionStatus.sendMessage(btConnectionBWStatusMsg);

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

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
            mmACKClientSocket.close();
            mmBWClientSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}

