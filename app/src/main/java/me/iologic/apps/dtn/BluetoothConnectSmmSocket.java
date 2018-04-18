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

class BluetoothConnectSmmSocket extends Thread {

    private final BluetoothServerSocket mmServerSocket;
    private final BluetoothServerSocket mmACKServerSocket;
    private final BluetoothServerSocket bandwidthSocket;
    private BluetoothSocket ClientSocket, AckSocketGlobal, BWSocketGlobal;
    public static final String TAG = "DTNLogs";
    public static final String NAME = "DTNApp";

    long pairingStartTime, pairingEndTime, duration;

    Handler btConnectionStatus;
    Message btConnectionStatusMsg;
    Message btConnectionACKStatusMsg;
    Message btConnectionBWStatusMsg;

    //UUIDs for second connection
    private static final UUID MY_UUID = Constants.UUIDs.mmSocket_UUID;
    private static final UUID ACK_UUID = UUID.fromString("928bef3c-e408-44f6-b339-06358055da16");
    private static final UUID BW_UUID = UUID.fromString("ddbb9433-d6c4-4fc5-b6a9-d96bdbc9d928");

    public BluetoothConnectSmmSocket(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus) {

        btConnectionStatus = getBtConnectionStatus;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        BluetoothServerSocket ACK_tmp = null;
        BluetoothServerSocket BW_tmp = null;

        try {
            // MY_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
            ACK_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, ACK_UUID);
            BW_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, BW_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
        mmACKServerSocket = ACK_tmp;
        bandwidthSocket = BW_tmp;

        btConnectionStatusMsg = Message.obtain();
        btConnectionACKStatusMsg = Message.obtain();
        btConnectionBWStatusMsg = Message.obtain();
    }

    public void run() {
        BluetoothSocket socket = null;
        BluetoothSocket AckSocket = null;
        BluetoothSocket BWSocket = null;
        ClientSocket = null;
        AckSocketGlobal = null;
        BWSocketGlobal = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                pairingStartTime = System.nanoTime();
                socket = mmServerSocket.accept();
                if (socket.isConnected()) {
                    pairingEndTime = System.nanoTime();
                }
                duration = (pairingEndTime - pairingStartTime);

                ClientSocket = socket;
                btConnectionStatusMsg.arg1 = 1;
                btConnectionStatusMsg.arg2 = (int) (duration / 1000000);
                btConnectionStatus.sendMessage(btConnectionStatusMsg);
                btConnectionStatusMsg = Message.obtain();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                btConnectionStatusMsg.arg1 = -1;
                btConnectionStatus.sendMessage(btConnectionStatusMsg);
                break;
            }

//            try {
//                BWSocket = bandwidthSocket.accept();
//                BWSocketGlobal = BWSocket;
//
//                btConnectionBWStatusMsg.arg1 = 3;
//                btConnectionStatus.sendMessage(btConnectionBWStatusMsg);
//                btConnectionBWStatusMsg = Message.obtain();
//            } catch (IOException e) {
//                Log.e(Constants.TAG, "BWSocket's accept() method failed", e);
//            }
//
//          /*  if (socket != null) {
//                // A connection was accepted. Perform work associated with
//                // the connection in a separate thread.
//                //  manageMyConnectedSocket(socket); (TBD)
//                try {
//                    mmServerSocket.close();
//                } catch (IOException e) {
//
//                    Log.e(TAG, "Could not close the connect socket", e);
//                }
//
//                break;
//            } */
//
//            // ACK Part
//
//            try {
//                AckSocket = mmACKServerSocket.accept();
//                AckSocketGlobal = AckSocket;
//
//                btConnectionACKStatusMsg.arg1 = 2;
//                btConnectionStatus.sendMessage(btConnectionACKStatusMsg);
//                btConnectionACKStatusMsg = Message.obtain();
//            } catch (IOException e) {
//                Log.e(Constants.TAG, "ACKSocket's accept() method failed", e);
//            }
        }
    }

    public BluetoothServerSocket get_mmsocket() {
        return mmServerSocket;
    }

    public BluetoothSocket getServerSocket() {
        return ClientSocket;
    }

    public BluetoothSocket getACKSocket() {
        return AckSocketGlobal;
    }

    public BluetoothSocket getBWSocket() {
        return BWSocketGlobal;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            if (ClientSocket != null) {
                ClientSocket.close();
            }

            if (AckSocketGlobal != null) {
                AckSocketGlobal.close();
            }

            if (BWSocketGlobal != null) {
                BWSocketGlobal.close();
            }

        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
}


