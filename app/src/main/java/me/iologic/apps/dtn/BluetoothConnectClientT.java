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

    boolean isAlreadyConnected;

    private static final UUID MY_UUID = UUID.fromString("085a7788-8a7e-4bb6-95e9-7c967912bf3f"); // UUID is uniquely generated
    private static final UUID ACK_UUID = UUID.fromString("928bef3c-e408-44f6-b339-06358055da16"); // UUID is uniquely generated
    private static final UUID BW_UUID = UUID.fromString("ddbb9433-d6c4-4fc5-b6a9-d96bdbc9d928"); // UUID is uniquely generated

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
            // Get a BluetoothSocket to connect with the given BluetoothDevice. Allowing Insecure connections to avoid Pairing Key.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            ACKtmp = device.createInsecureRfcommSocketToServiceRecord(ACK_UUID);
            BWtmp = device.createInsecureRfcommSocketToServiceRecord(BW_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
        mmACKClientSocket = ACKtmp;
        mmBWClientSocket = BWtmp;

        isAlreadyConnected = false;

        btConnectionStatusMsg = Message.obtain();
        btConnectionACKStatusMsg = Message.obtain();
        btConnectionBWStatusMsg = Message.obtain();
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

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

        Thread firstClientConnectT = new Thread() {
            public void run() {
                //while (true) {
                clientConnect();
                btConnectionStatusMsg = Message.obtain();
                //}
            }
        };

        firstClientConnectT.start();

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
    }

    public void clientConnect() {
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
            closemmSocket();
        }

        if (mmSocket.isConnected()) {
            isAlreadyConnected = true;
            btConnectionStatusMsg.arg1 = 1;
            btConnectionStatusMsg.arg2 = (int) (duration / 1000000);

            btConnectionStatus.sendMessage(btConnectionStatusMsg);
        }
    }

    public boolean checkIfmmSocketIsConnected() {
        // Log.i(Constants.TAG, "Checking: " + mmSocket.isConnected() + " " + isAlreadyConnected);
        if (mmSocket.isConnected() ^ isAlreadyConnected) {
            return true;
        } else {
            return false;
        }

    }

    public void closemmSocket() {
        try {
            mmSocket.close();
        } catch (IOException error) {
            Log.i(Constants.TAG, "mmSocket is disconnected");
        }
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
            if (mmSocket != null) {
                mmSocket.close();
            }
            if (mmACKClientSocket != null) {
                mmACKClientSocket.close();
            }

            if (mmBWClientSocket != null) {
                mmBWClientSocket.close();
            }

        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}

