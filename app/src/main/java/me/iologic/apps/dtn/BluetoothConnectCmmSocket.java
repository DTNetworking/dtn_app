package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnectCmmSocket extends Thread {

    private final BluetoothDevice mmDevice;
    private final BluetoothSocket mmSocket;

    Handler btConnectionStatus;
    Message btConnectionStatusMsg;

    long pairingStartTime, pairingEndTime, duration;

    public BluetoothConnectCmmSocket(BluetoothDevice device, Handler getBtConnectionStatus, UUIDManager receivedDeviceUUID) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;

        mmDevice = device;
        btConnectionStatus = getBtConnectionStatus;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice. Allowing Insecure connections to avoid Pairing Key.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createInsecureRfcommSocketToServiceRecord(receivedDeviceUUID.mmSocket_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Socket's create() method failed", e);
        }

        mmSocket = tmp;

        btConnectionStatusMsg = Message.obtain();

    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            pairingStartTime = System.nanoTime();
            mmSocket.connect();
            pairingEndTime = System.nanoTime();
            duration = (pairingEndTime - pairingStartTime);

        } catch (IOException connectException) {
            btConnectionStatusMsg = Message.obtain();
            btConnectionStatusMsg.arg1 = -1;
            btConnectionStatus.sendMessage(btConnectionStatusMsg);
        }

        btConnectionStatusMsg = Message.obtain();

        btConnectionStatusMsg.arg1 = 1;
        btConnectionStatusMsg.arg2 = (int) (duration / 1000000);

        btConnectionStatus.sendMessage(btConnectionStatusMsg);

    }

    public BluetoothSocket getClientSocket() {
        return mmSocket;
    }

    public void cancel() {
        try {
            if (mmSocket != null) {
                mmSocket.close();
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the MSG client socket", e);
        }
    }


}
