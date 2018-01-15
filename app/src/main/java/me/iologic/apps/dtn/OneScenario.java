package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class OneScenario extends AppCompatActivity {

    static final int REQUEST_ENABLE_BT=1;
    BluetoothAdapter mBluetoothAdapter; // The Only Bluetooth Adapter Used.
    int noOfPeers = 0;
    BluetoothConnectT serverConnect;
    BluetoothConnectClientT clientConnect;
    BluetoothDevice btDeviceConnectedGlobal;
    ArrayList<BluetoothDevice> btDevicesFoundList = new ArrayList<BluetoothDevice>(); // Store list of bluetooth devices.
    Handler btClientConnectionStatus;
    Handler btServerConnectionStatus;

    public static String SERVER_CONNECTION_SUCCESSFUL;
    public static String SERVER_CONNECTION_FAIL;

    public static String CLIENT_CONNECTION_SUCCESSFUL;
    public static String CLIENT_CONNECTION_FAIL;

    TextView btStatusText;
    TextView peerStatusText;
    TextView messageReceived;

    boolean toastShown = false; // Client Re-Connection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_scenario);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btStatusText = (TextView) findViewById(R.id.btStatus);
        peerStatusText = (TextView) findViewById(R.id.peerStatus);
        messageReceived = (TextView) findViewById(R.id.messageStatus);

        btStatusText.setSelected(true); // For Horizontal Scrolling
        messageReceived.setSelected(true); // For Horizontal Scrolling

        btServerConnectionStatus = new Handler();
        btClientConnectionStatus = new Handler();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        startBluetooth();
    }

    protected void startBluetooth()
    {
        String discMessage = "Discoverability set to ON";
        String btEnabledMessage = "Bluetooth is Enabled";
        Toast btDeviceDiscoverToast = Toast.makeText(getApplicationContext(), discMessage, Toast.LENGTH_SHORT);
        btDeviceDiscoverToast.show();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                btStatusText.setText("Bluetooth Not Found!");
            } else if (!mBluetoothAdapter.isEnabled()) {
               // mBluetoothAdapter.enable();
         //   Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

          //  startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); // Calls onActivityResult */
                setBtName();

                Toast btDeviceEnableToast = Toast.makeText(getApplicationContext(), btEnabledMessage, Toast.LENGTH_SHORT);
                btDeviceEnableToast.show();

                Handler qBhandler = new Handler();
                qBhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        discBluetoothDevices();
                    }
                }, 2000);

            } else if (mBluetoothAdapter.isEnabled()) {
                btStatusText.setText("Bluetooth is already enabled!");
                setBtName();
                Handler qBhandler = new Handler();
                qBhandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        discBluetoothDevices();
                    }
                }, 2000);

            }

    }

    public void setBtName(){
        String btDeviceName = "DTN-"+ Build.SERIAL;
        String message = "Bluetooth Device Name: " + btDeviceName;
        mBluetoothAdapter.setName(btDeviceName);
        Toast btDeviceNameToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        btDeviceNameToast.show();

        serverConnection(); // Let's start the Server
    }

    protected void discBluetoothDevices() {
        if(mBluetoothAdapter.isDiscovering())
        {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
        btStatusText.setText("Discovering Bluetooth Devices...");
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btDevicesFoundList.add(device);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                String discBDevice = "Found Device: " + deviceName;
                noOfPeers++;
                Toast toast = Toast.makeText(getApplicationContext(), discBDevice, Toast.LENGTH_SHORT);
                toast.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                btStatusText.setText("Discovery Period Finished");
                connectDevice();
            }
            peerStatusText.setText("No of Peers Found: " + noOfPeers);
        }
    };

    public void connectDevice(){

        String btDeviceName = "DTN-";
        CLIENT_CONNECTION_FAIL = "Connection Failed!";

        btClientConnectionStatus = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.arg1 == 1)
                {
                    Toast toast = Toast.makeText(getApplicationContext(), CLIENT_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT);
                    toast.show();
                } else if(msg.arg1 == -1){
                    if(toastShown == false) {
                        Toast toast = Toast.makeText(getApplicationContext(), CLIENT_CONNECTION_FAIL, Toast.LENGTH_SHORT);
                        toast.show();
                       // clientConnect.start(); // Keep Trying To Connect If It Fails
                    }

                    toastShown = true;
                    // clientConnect.start(); // Keep Trying To Connect If It Fails
                }
            }
        };

        for(BluetoothDevice btDevice : btDevicesFoundList) {
            if (btDevice.getName().contains(btDeviceName)) {
                btDeviceConnectedGlobal = btDevice;
                clientConnect = new BluetoothConnectClientT(btDevice, mBluetoothAdapter, btClientConnectionStatus);
                clientConnect.start();
            }

        }
            if(!(btDeviceConnectedGlobal == null)) {
                CLIENT_CONNECTION_SUCCESSFUL = "Connected To:" + btDeviceConnectedGlobal.getName();
            } else {
                Log.e("DTN", "No Device Found With Name DTN");
            }
    }

    private void serverConnection(){

        SERVER_CONNECTION_SUCCESSFUL ="Server is successfully connected!";
        SERVER_CONNECTION_FAIL = "Server failed to connect";

        btServerConnectionStatus = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.arg1 == 1)
                {
                    Toast toast = Toast.makeText(getApplicationContext(), SERVER_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT);
                    toast.show();
                } else if(msg.arg1 == -1){
                    Toast toast = Toast.makeText(getApplicationContext(), SERVER_CONNECTION_FAIL, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        };

        serverConnect = new BluetoothConnectT(mBluetoothAdapter, btServerConnectionStatus);
        serverConnect.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }



}
