package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;
import android.os.Handler;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by vinee on 15-01-2018.
 */

class BluetoothConnectClientT extends Thread{
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    BluetoothAdapter mBluetoothAdapter;

    public static final String TAG = "DTNLogs";
    Handler btConnectionStatus;
    Message btConnectionStatusMsg;

    int isAlreadyConnected = 0;
    long pairingStartTime, pairingEndTime, duration;

    private static final UUID MY_UUID = UUID.fromString("6e7bd336-5676-407e-a41c-0691e1964345"); // UUID is uniquely generated

    public BluetoothConnectClientT(BluetoothDevice device, BluetoothAdapter getBluetoothAdapter, Handler getBtConnectionStatus) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        mBluetoothAdapter = getBluetoothAdapter;
        btConnectionStatus = getBtConnectionStatus;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

        btConnectionStatusMsg = Message.obtain();

        while((mmSocket.isConnected()!=true) && isAlreadyConnected!=1) {

            Log.i("DTNRunning", "I am running. " + isAlreadyConnected);

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
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            isAlreadyConnected++;

            btConnectionStatusMsg.arg1 = 1;
            btConnectionStatusMsg.arg2 = (int) (duration / 1000000);

            btConnectionStatus.sendMessage(btConnectionStatusMsg);

        }
    }

    public BluetoothSocket getClientSocket()
    {
        return mmSocket;
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}

