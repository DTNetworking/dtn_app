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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class OneScenario extends AppCompatActivity {

    static final int REQUEST_ENABLE_BT=1;
    BluetoothAdapter mBluetoothAdapter; // The Only Bluetooth Adapter Used.
    int noOfPeers = 0;

    TextView btStatusText;
    TextView peerStatusText;

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
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                String discBDevice = "Found Device: " + deviceName;
                noOfPeers++;
                Toast toast = Toast.makeText(getApplicationContext(), discBDevice, Toast.LENGTH_SHORT);
                toast.show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                btStatusText.setText("Discovery Period Finished");
            }
            peerStatusText.setText("No of Peers Found: " + noOfPeers);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }



}
