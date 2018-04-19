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

public class BluetoothConnectSsecondmmSocket extends Thread {

    private final BluetoothServerSocket secondMMServerSocket;
    private BluetoothSocket ServerSocket;

    long pairingStartTime, pairingEndTime, duration;

    Handler secondBtConnectionStatus;
    Message secondBtConnectionStatusMsg;

    public BluetoothConnectSsecondmmSocket(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus) {

        secondBtConnectionStatus = getBtConnectionStatus;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket second_tmp = null;

        try {
            // MY_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            second_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.NAME, Constants.secondUUIDs.secondMMSocket_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Socket's listen() method failed", e);
        }
        secondMMServerSocket = second_tmp;

        secondBtConnectionStatusMsg = Message.obtain();
    }

    public void run() {
        BluetoothSocket socket;
        ServerSocket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                pairingStartTime = System.nanoTime();
                socket = secondMMServerSocket.accept();
                if (socket.isConnected()) {
                    pairingEndTime = System.nanoTime();
                }
                duration = (pairingEndTime - pairingStartTime);

                ServerSocket = socket;
                secondBtConnectionStatusMsg.arg1 = 1;
                secondBtConnectionStatusMsg.arg2 = (int) (duration / 1000000);
                secondBtConnectionStatus.sendMessage(secondBtConnectionStatusMsg);
                secondBtConnectionStatusMsg = Message.obtain();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Socket's accept() method failed", e);
                secondBtConnectionStatusMsg.arg1 = -1;
                secondBtConnectionStatus.sendMessage(secondBtConnectionStatusMsg);
            }

            return;
        }
    }

    public BluetoothSocket getServerSocket() {
        return ServerSocket;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            if (secondMMServerSocket != null) {
                secondMMServerSocket.close();
            }

        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}

