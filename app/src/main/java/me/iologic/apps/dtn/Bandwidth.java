package me.iologic.apps.dtn;

import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;

/**
 * Created by Abhishanth Padarthy on 30-01-2018.
 */

public class Bandwidth extends Thread {

    private final BluetoothSocket bandwidthSocket;
    private final InputStream bandwidthInStream;
    private final OutputStream bandwidthOutStream;
    private byte[] bandwidthBuffer; // mmBuffer store ACK bytes for the stream

    private Handler bandwidthHandler;

    @Override
    public void run() {

    }
}
