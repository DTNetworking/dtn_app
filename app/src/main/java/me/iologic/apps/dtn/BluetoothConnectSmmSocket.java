package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.CpuUsageInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by vinee on 15-01-2018.
 */

class BluetoothConnectSmmSocket extends Thread {

    private final BluetoothServerSocket mmServerSocket;

    private BluetoothSocket ServerSocket;

    long pairingStartTime, pairingEndTime, duration;

    Handler btConnectionStatus;
    Message btConnectionStatusMsg;

    public BluetoothConnectSmmSocket(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus) {

        btConnectionStatus = getBtConnectionStatus;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;

        try {
            // MY_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.NAME, Constants.UUIDs.mmSocket_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;

        btConnectionStatusMsg = Message.obtain();
    }

    public void run() {
        BluetoothSocket socket;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                pairingStartTime = System.nanoTime();
                socket = mmServerSocket.accept();
                if (socket.isConnected()) {
                    pairingEndTime = System.nanoTime();
                }
                duration = (pairingEndTime - pairingStartTime);

                ServerSocket = socket;
                btConnectionStatusMsg.arg1 = 1;
                btConnectionStatusMsg.arg2 = (int) (duration / 1000000);
                btConnectionStatus.sendMessage(btConnectionStatusMsg);
                btConnectionStatusMsg = Message.obtain();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Socket's accept() method failed", e);
                btConnectionStatusMsg.arg1 = -1;
                btConnectionStatus.sendMessage(btConnectionStatusMsg);
            }
        }
    }

    public BluetoothSocket getServerSocket() {

        if(ServerSocket!=null){
            Log.i(Constants.TAG, "NOT NULL");
            return ServerSocket;
        } else {
            Log.e(Constants.TAG, "IS NULL");
        }

        return null;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            if (mmServerSocket != null) {
                mmServerSocket.close();
            }

        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}

