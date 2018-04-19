package me.iologic.apps.dtn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectCACKSocket extends Thread {

    private final BluetoothDevice mmDevice;
    private final BluetoothSocket mmACKClientSocket;

    Handler btConnectionStatus;
    Message btConnectionACKStatusMsg;

    public BluetoothConnectCACKSocket(BluetoothDevice device, Handler getBtConnectionStatus) {
        BluetoothSocket ACKtmp = null;

        mmDevice = device;
        btConnectionStatus = getBtConnectionStatus;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice. Allowing Insecure connections to avoid Pairing Key.
            // ACK_UUID is the app's UUID string, also used in the server code.
            ACKtmp = device.createInsecureRfcommSocketToServiceRecord(Constants.UUIDs.ACK_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "ACKSocket's create() method failed", e);
        }

        mmACKClientSocket = ACKtmp;

        btConnectionACKStatusMsg = Message.obtain();
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        try {
            mmACKClientSocket.connect();
        } catch (IOException e) {
            btConnectionACKStatusMsg.arg1 = -2;
            btConnectionStatus.sendMessage(btConnectionACKStatusMsg);
            Log.e(Constants.TAG, "I could not connect to ACK Socket on the server side");
            try {
                mmACKClientSocket.close();
            } catch (IOException closeException) {
                Log.e(Constants.TAG, "Could not close the ACK client socket", closeException);

            }
        }

        btConnectionACKStatusMsg = Message.obtain();
        btConnectionACKStatusMsg.arg1 = 2;
        btConnectionStatus.sendMessage(btConnectionACKStatusMsg);
    }

    public BluetoothSocket getACKClientSocket() {
        return mmACKClientSocket;
    }


    public void cancel() {
        try {
            if (mmACKClientSocket != null) {
                mmACKClientSocket.close();
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the client socket", e);
        }
    }

}
