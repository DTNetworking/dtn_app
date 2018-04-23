package me.iologic.apps.dtn;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectCBWSocket extends Thread {

    private final BluetoothDevice mmDevice;
    private final BluetoothSocket mmBWClientSocket;

    Handler btConnectionStatus;
    Message btConnectionBWStatusMsg;

    public BluetoothConnectCBWSocket(BluetoothDevice device, Handler getBtConnectionStatus, UUIDManager receivedDeviceUUID) {
        BluetoothSocket BWtmp = null;

        mmDevice = device;
        btConnectionStatus = getBtConnectionStatus;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice. Allowing Insecure connections to avoid Pairing Key.
            // BW_UUID is the app's UUID string, also used in the server code.
            BWtmp = device.createInsecureRfcommSocketToServiceRecord(receivedDeviceUUID.BW_UUID);
        } catch (IOException e) {
            Log.e(Constants.TAG, "ACKSocket's create() method failed", e);
        }

        mmBWClientSocket = BWtmp;

        btConnectionBWStatusMsg = Message.obtain();
    }

    public void run() {
        try {
            mmBWClientSocket.connect();
        } catch (IOException e) {
            btConnectionBWStatusMsg.arg1 = -2;
            btConnectionStatus.sendMessage(btConnectionBWStatusMsg);
            Log.e(Constants.TAG, "I could not connect to BW Socket on the server side");
            try {
                mmBWClientSocket.close();
            } catch (IOException closeException) {
                Log.e(Constants.TAG, "Could not close the client BW socket", closeException);
            }
        }

        btConnectionBWStatusMsg = Message.obtain();
        btConnectionBWStatusMsg.arg1 = 100;
        btConnectionStatus.sendMessage(btConnectionBWStatusMsg);
    }

    public BluetoothSocket getBWClientSocket() {
        return mmBWClientSocket;
    }

    public void cancel() {
        try {
            if (mmBWClientSocket != null) {
                mmBWClientSocket.close();
            }
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not close the client socket", e);
        }
    }
}
