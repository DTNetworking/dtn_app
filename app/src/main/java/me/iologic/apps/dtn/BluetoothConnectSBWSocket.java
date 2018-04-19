package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectSBWSocket extends Thread {

    private final BluetoothServerSocket bandwidthSocket;

    private BluetoothSocket BWSocketGlobal;

    Handler btConnectionStatus;
    Message btConnectionBWStatusMsg;

    public BluetoothConnectSBWSocket(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus) {
        btConnectionStatus = getBtConnectionStatus;

        BluetoothServerSocket BW_tmp = null;

        try {
            // BW_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            BW_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.NAME, Constants.UUIDs.BW_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Socket's listen() method failed", e);
        }

        bandwidthSocket = BW_tmp;

        btConnectionBWStatusMsg = Message.obtain();
    }

    public void run() {
        BluetoothSocket BWSocket;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                BWSocket = bandwidthSocket.accept();
                BWSocketGlobal = BWSocket;

                btConnectionBWStatusMsg.arg1 = 3;
                btConnectionStatus.sendMessage(btConnectionBWStatusMsg);
                btConnectionBWStatusMsg = Message.obtain();
            } catch (IOException e) {
                Log.e(Constants.TAG, "BWSocket's accept() method failed", e);
            }
        }
    }

    public BluetoothSocket getBWSocket() {
        return BWSocketGlobal;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {

            if (BWSocketGlobal != null) {
                BWSocketGlobal.close();
            }

        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the connect socket", e);
        }
    }
}