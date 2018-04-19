package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectSsecondACKSocket extends Thread {
    private final BluetoothServerSocket secondMMACKServerSocket;
    private BluetoothSocket AckSocketGlobal;

    Handler secondBtConnectionStatus;
    Message secondBtConnectionACKStatusMsg;

    public BluetoothConnectSsecondACKSocket(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus) {

        secondBtConnectionStatus = getBtConnectionStatus;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket second_ACK_tmp = null;

        try {
            // MY_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            second_ACK_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.NAME, Constants.UUIDs.second_ACK_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Socket's listen() method failed", e);
        }
        secondMMACKServerSocket = second_ACK_tmp;

        secondBtConnectionACKStatusMsg = Message.obtain();
    }

    public void run() {
        BluetoothSocket AckSocket;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                AckSocket = secondMMACKServerSocket.accept();
                AckSocketGlobal = AckSocket;

                secondBtConnectionACKStatusMsg.arg1 = 2;
                secondBtConnectionStatus.sendMessage(secondBtConnectionACKStatusMsg);
                secondBtConnectionACKStatusMsg = Message.obtain();
            } catch (IOException e) {
                Log.e(Constants.TAG, "ACKSocket's accept() method failed", e);
            }

            return;
        }
    }

    public BluetoothSocket getACKSocket() {
        return AckSocketGlobal;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            if (secondMMACKServerSocket != null) {
                secondMMACKServerSocket.close();
            }

        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }


}