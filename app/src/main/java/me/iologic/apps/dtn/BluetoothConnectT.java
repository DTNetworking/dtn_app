package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by vinee on 15-01-2018.
 */

class BluetoothConnectT extends Thread {

    private final BluetoothServerSocket mmServerSocket;
    private BluetoothSocket ClientSocket;
    public static final String TAG = "DTNLogs";
    public static final String NAME = "DTNApp";

    long pairingStartTime, pairingEndTime, duration;

    Handler btConnectionStatus;
    Message btConnectionStatusMsg;

    private static final UUID MY_UUID = UUID.fromString("6e7bd336-5676-407e-a41c-0691e1964345"); // UUID is uniquely generated

    public BluetoothConnectT(BluetoothAdapter mBluetoothAdapter,Handler getBtConnectionStatus){

        btConnectionStatus = getBtConnectionStatus;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        ClientSocket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                pairingStartTime = System.nanoTime();
                socket = mmServerSocket.accept();
                if(socket.isConnected()) {
                    pairingEndTime = System.nanoTime();
                }
                duration = (pairingEndTime - pairingStartTime);

                ClientSocket = socket;
                btConnectionStatusMsg = Message.obtain();
                btConnectionStatusMsg.arg1 = 1;
                btConnectionStatusMsg.arg2 = (int)(duration/1000000);
                btConnectionStatus.sendMessage(btConnectionStatusMsg);

            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                btConnectionStatusMsg.arg1 = -1;
                btConnectionStatus.sendMessage(btConnectionStatusMsg);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                //  manageMyConnectedSocket(socket); (TBD)
                try {
                    mmServerSocket.close();
                } catch (IOException e) {

                    Log.e(TAG, "Could not close the connect socket", e);
                }

                break;
            }
        }
    }

    public BluetoothServerSocket get_mmsocket()
    {
        return mmServerSocket;
    }

    public BluetoothSocket getClientSocket()
    {
        return ClientSocket;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}


