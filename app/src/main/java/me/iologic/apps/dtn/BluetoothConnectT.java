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
    private final BluetoothServerSocket mmACKServerSocket;
    private final BluetoothServerSocket bandwidthSocket;
    //for 2nd connection
    private final BluetoothServerSocket secondMMServerSocket;
    private final BluetoothServerSocket secondMMACKServerSocket;
    private final BluetoothServerSocket secondBandwidthSocket;
    private BluetoothSocket ClientSocket, AckSocketGlobal, BWSocketGlobal;
    private BluetoothSocket secondClientSocket, secondAckSocketGlobal, secondBWSocketGlobal;//for 2nd connection
    public static final String TAG = "DTNLogs";
    public static final String NAME = "DTNApp";

    long pairingStartTime, pairingEndTime, duration, secondPairingStartTime, secondPairingEndTime, secondDuration;

    Handler btConnectionStatus;
    Message btConnectionStatusMsg;
    Message btConnectionACKStatusMsg;
    Message btConnectionBWStatusMsg;

    //for 2nd connection
    Handler secondBtConnectionStatus;
    Message secondBtConnectionStatusMsg;
    Message secondBtConnectionACKStatusMsg;
    Message secondBtConnectionBWStatusMsg;

    private static final UUID MY_UUID = UUID.fromString("085a7788-8a7e-4bb6-95e9-7c967912bf3f"); // UUID is uniquely generated
    private static final UUID ACK_UUID = UUID.fromString("928bef3c-e408-44f6-b339-06358055da16"); // UUID is uniquely generated
    private static final UUID BW_UUID = UUID.fromString("ddbb9433-d6c4-4fc5-b6a9-d96bdbc9d928"); // UUID is uniquely generated

    //UUIDs for second connection
    private static final UUID MY_SECOND_UUID = UUID.fromString("fa249bcd-e53c-4965-a9f9-d7ea5d6f0040");
    private static final UUID SECOND_ACK_UUID = UUID.fromString("d9c13848-d7be-48a1-ac11-5f0c082791c7");
    private static final UUID SECOND_BW_UUID = UUID.fromString("5c6ae5f9-cb04-4a71-9552-ffe426b02b99");

    public BluetoothConnectT(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus) {

        btConnectionStatus = getBtConnectionStatus;
        secondBtConnectionStatus = getBtConnectionStatus;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        BluetoothServerSocket tmp = null;
        BluetoothServerSocket ACK_tmp = null;
        BluetoothServerSocket BW_tmp = null;

        //temporary object for second connection
        BluetoothServerSocket second_tmp = null;
        BluetoothServerSocket second_ACK_tmp = null;
        BluetoothServerSocket second_BW_tmp = null;

        try {
            // MY_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_UUID);
            ACK_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, ACK_UUID);
            BW_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, BW_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }

        //for 2nd connection
        try {
            // MY_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            second_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_SECOND_UUID);
            second_ACK_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, SECOND_ACK_UUID);
            second_BW_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, SECOND_BW_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed during second connection", e);
        }

        mmServerSocket = tmp;
        mmACKServerSocket = ACK_tmp;
        bandwidthSocket = BW_tmp;

        //second connection initialization
        secondMMServerSocket = second_tmp;
        secondMMACKServerSocket = second_ACK_tmp;
        secondBandwidthSocket = second_BW_tmp;

        btConnectionStatusMsg = Message.obtain();
        btConnectionACKStatusMsg = Message.obtain();
        btConnectionBWStatusMsg = Message.obtain();

        //for 2nd connection
        secondBtConnectionStatusMsg = Message.obtain();
        secondBtConnectionACKStatusMsg = Message.obtain();
        secondBtConnectionBWStatusMsg = Message.obtain();
    }

    public void run() {
        BluetoothSocket socket = null;
        BluetoothSocket AckSocket = null;
        BluetoothSocket BWSocket = null;
        ClientSocket = null;
        AckSocketGlobal = null;
        BWSocketGlobal = null;

        //for 2nd connection
        BluetoothSocket secondSocket = null;
        BluetoothSocket secondAckSocket = null;
        BluetoothSocket secondBWSocket = null;
        secondClientSocket = null;
        secondAckSocketGlobal = null;
        secondBWSocketGlobal = null;

        // Keep listening until exception occurs or a socket is returned.
        while (true) {
    /*        try {
                BWSocket = bandwidthSocket.accept();
                BWSocketGlobal = BWSocket;
                btConnectionBWStatusMsg.arg1 = 3;
                btConnectionStatus.sendMessage(btConnectionBWStatusMsg);
            } catch (IOException e) {
                Log.e(Constants.TAG, "BWSocket's accept() method failed", e);
            }
            //for 2nd connection
            try {
                secondBWSocket = secondBandwidthSocket.accept();
                secondBWSocketGlobal = secondBWSocket;
                secondBtConnectionBWStatusMsg.arg1 = 10;
                secondBtConnectionStatus.sendMessage(secondBtConnectionBWStatusMsg);
            } catch (IOException e) {
                Log.e(Constants.TAG, "secondBWSocket's accept() method failed", e);
            } */

          /*  try {
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

            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                btConnectionStatusMsg.arg1 = -1;
                btConnectionStatus.sendMessage(btConnectionStatusMsg);
                break;
            } */

            //for 2nd connection
            try {
                secondPairingStartTime = System.nanoTime();
                secondSocket = secondMMServerSocket.accept();
                if (secondSocket.isConnected()) {
                    secondPairingEndTime = System.nanoTime();
                }
                secondDuration = (secondPairingEndTime - secondPairingStartTime);

                secondClientSocket = secondSocket;
                secondBtConnectionStatusMsg.arg1 = 8;
                secondBtConnectionStatusMsg.arg2 = (int) (duration / 1000000);
                secondBtConnectionStatus.sendMessage(secondBtConnectionStatusMsg);

            } catch (IOException e) {
                Log.e(TAG, "Second Socket's accept() method failed", e);
                secondBtConnectionStatusMsg.arg1 = -1;
                secondBtConnectionStatus.sendMessage(secondBtConnectionStatusMsg);
                break;
            }

          /*  if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                //  manageMyConnectedSocket(socket); (TBD)
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
                break;
            } */

            // ACK Part

        /*    try {
                AckSocket = mmACKServerSocket.accept();
                AckSocketGlobal = AckSocket;

                btConnectionACKStatusMsg.arg1 = 2;
                btConnectionStatus.sendMessage(btConnectionACKStatusMsg);
            } catch (IOException e) {
                Log.e(Constants.TAG, "ACKSocket's accept() method failed", e);
            } */

            //for 2nd connection
            try {
                secondAckSocket = secondMMACKServerSocket.accept();
                secondAckSocketGlobal = secondAckSocket;

                secondBtConnectionACKStatusMsg.arg1 = 9;
                secondBtConnectionStatus.sendMessage(secondBtConnectionACKStatusMsg);
            } catch (IOException e) {
                Log.e(Constants.TAG, "Second ACKSocket's accept() method failed", e);
            }
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

    //for 2nd connection
    public BluetoothServerSocket get_secondMMsocket() {
        return secondMMServerSocket;
    }

    public BluetoothSocket getSecondServerSocket() {
        return secondClientSocket;
    }

    public BluetoothSocket getSecondACKSocket() {
        return secondAckSocketGlobal;
    }

    public BluetoothSocket getSecondBWSocket() {
        return secondBWSocketGlobal;
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
            mmACKServerSocket.close();
            bandwidthSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }

        //for 2nd connection
        try {
            secondMMServerSocket.close();
            secondMMACKServerSocket.close();
            secondBandwidthSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the second connect socket", e);
        }
    }
}