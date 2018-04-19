package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectSsecondBWSocket extends Thread {

    private final BluetoothServerSocket secondBandwidthSocket;

    private BluetoothSocket BWSocketGlobal;

    Handler secondBtConnectionStatus;
    Message secondBtConnectionBWStatusMsg;

    public BluetoothConnectSsecondBWSocket(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus) {
        secondBtConnectionStatus = getBtConnectionStatus;

        BluetoothServerSocket second_BW_tmp = null;

        try {
            // BW_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            second_BW_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.NAME, Constants.UUIDs.second_BW_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Socket's listen() method failed", e);
        }

        secondBandwidthSocket = second_BW_tmp;

        secondBtConnectionBWStatusMsg = Message.obtain();
    }

    public void run() {
        BluetoothSocket socket;
        BluetoothSocket BWSocket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                BWSocket = secondBandwidthSocket.accept();
                BWSocketGlobal = BWSocket;

                secondBtConnectionBWStatusMsg.arg1 = 3;
                secondBtConnectionStatus.sendMessage(secondBtConnectionBWStatusMsg);
                secondBtConnectionBWStatusMsg = Message.obtain();
            } catch (IOException e) {
                Log.e(Constants.TAG, "BWSocket's accept() method failed", e);
            }

            return;
        }
    }

    public BluetoothSocket getBWSocket() {
        return BWSocketGlobal;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {

            if (secondBandwidthSocket != null) {
                secondBandwidthSocket.close();
            }

        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}