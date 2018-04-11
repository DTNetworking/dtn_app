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

class BluetoothSecondConnectT extends Thread {

    //for 2nd connection
    private final BluetoothServerSocket secondMMServerSocket;
    private final BluetoothServerSocket secondMMACKServerSocket;
    private final BluetoothServerSocket secondBandwidthSocket;
    private BluetoothSocket ClientSocket, AckSocketGlobal, BWSocketGlobal;
    private BluetoothSocket secondClientSocket, secondAckSocketGlobal, secondBWSocketGlobal;//for 2nd connection
    public static final String TAG = "DTNLogs";
    public static final String NAME = "DTNApp";

    long duration, secondPairingStartTime, secondPairingEndTime, secondDuration;

    //for 2nd connection
    Handler secondBtConnectionStatus;
    Message secondBtConnectionStatusMsg;
    Message secondBtConnectionACKStatusMsg;
    Message secondBtConnectionBWStatusMsg;

    //UUIDs for second connection
    private static final UUID MY_SECOND_UUID = UUID.fromString("085a7788-8a7e-4bb6-95e9-7c967912bf3f");
    private static final UUID SECOND_ACK_UUID = UUID.fromString("928bef3c-e408-44f6-b339-06358055da16");
    private static final UUID SECOND_BW_UUID = UUID.fromString("ddbb9433-d6c4-4fc5-b6a9-d96bdbc9d928");


    public BluetoothSecondConnectT(BluetoothAdapter mBluetoothAdapter, Handler getBtConnectionStatus) {

        secondBtConnectionStatus = getBtConnectionStatus;
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.

        //temporary object for second connection
        BluetoothServerSocket second_tmp = null;
        BluetoothServerSocket second_ACK_tmp = null;
        BluetoothServerSocket second_BW_tmp = null;

        //for 2nd connection
        try {
            // MY_UUID is the app's UUID string, also used by the client code. Allowing Insecure connections to avoid Pairing Key.
            second_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, MY_SECOND_UUID);
            second_ACK_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, SECOND_ACK_UUID);
            second_BW_tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, SECOND_BW_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed during second connection", e);
        }

        //second connection initialization
        secondMMServerSocket = second_tmp;
        secondMMACKServerSocket = second_ACK_tmp;
        secondBandwidthSocket = second_BW_tmp;

        //for 2nd connection
        secondBtConnectionStatusMsg = Message.obtain();
        secondBtConnectionACKStatusMsg = Message.obtain();
        secondBtConnectionBWStatusMsg = Message.obtain();
    }

    public void run() {
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

            //for 2nd connection
            /* try {
                secondBWSocket = secondBandwidthSocket.accept();
                secondBWSocketGlobal = secondBWSocket;

                secondBtConnectionBWStatusMsg.arg1 = 10;
                secondBtConnectionStatus.sendMessage(secondBtConnectionBWStatusMsg);
            } catch (IOException e) {
                Log.e(Constants.TAG, "secondBWSocket's accept() method failed", e);
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


