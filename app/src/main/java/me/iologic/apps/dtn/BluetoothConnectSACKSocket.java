package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectSACKSocket extends Thread {
    private final BluetoothServerSocket mmACKServerSocket;
    private BluetoothSocket AckSocketGlobal;

    Handler btConnectionStatus;
    Message btConnectionACKStatusMsg;

    public BluetoothConnectSACKSocket(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus, UUIDManager receivedDeviceUUID) {

        btConnectionStatus = getBtConnectionStatus;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket ACK_tmp = null;

        try {
            // MY_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            ACK_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.NAME, receivedDeviceUUID.ACK_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Socket's listen() method failed", e);
        }
        mmACKServerSocket = ACK_tmp;

        btConnectionACKStatusMsg = Message.obtain();
    }

    public void run() {
        BluetoothSocket AckSocket;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                AckSocket = mmACKServerSocket.accept();
                AckSocketGlobal = AckSocket;

                btConnectionACKStatusMsg.arg1 = 2;
                btConnectionStatus.sendMessage(btConnectionACKStatusMsg);
                btConnectionACKStatusMsg = Message.obtain();
            } catch (IOException e) {
                Log.e(Constants.TAG, "ACKSocket's accept() method failed", e);
            }
        }
    }

    public BluetoothSocket getACKSocket() {
        return AckSocketGlobal;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            if (mmACKServerSocket != null) {
                mmACKServerSocket.close();
            }

        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }


}