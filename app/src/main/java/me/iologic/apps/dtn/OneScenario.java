package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class OneScenario extends AppCompatActivity {

    static final int REQUEST_ENABLE_BT = 1;
    BluetoothAdapter mBluetoothAdapter; // The Only Bluetooth Adapter Used.
    boolean connectAsClient = true;
    int noOfPeers = 0;
    BluetoothConnectT serverConnect;
    BluetoothConnectClientT clientConnect;
    BluetoothBytesT streamData;
    BandwidthBytesT bandData;
    BluetoothACKBytesT ACKData;
    BluetoothDevice btDeviceConnectedGlobal; // To get Device Name
    BluetoothSocket SocketGlobal; // To store MAIN socket
    BluetoothSocket ACKSocketGlobal; // To store ACK socket
    BluetoothSocket BandSocketGlobal; // To store Bandwidth Socket
    ArrayList<BluetoothDevice> btDevicesFoundList = new ArrayList<BluetoothDevice>(); // Store list of bluetooth devices.
    String getGoodOldName;

    Handler btClientConnectionStatus;
    Handler btServerConnectionStatus;
    Bundle bundle;

    FileServices useFile;
    File tempFile;

    double FileSentBandwidth;
    Handler getDataHandler;
    boolean deviceConnected;
    Handler retryConnectionHandler = new Handler();

    private static String SERVER_CONNECTION_SUCCESSFUL;
    private static String SERVER_CONNECTION_FAIL;

    private static String CLIENT_CONNECTION_SUCCESSFUL;
    private static String CLIENT_CONNECTION_FAIL;

    private static String NOT_YET_CONNECTED;

    TextView btStatusText;
    TextView peerStatusText;
    TextView messageReceived;
    TextView currentStatusText;
    TextView peerConnectTime;
    TextView speedText;
    TextView delayText;
    TextView delayMs;
    EditText EditMessageBox;
    Button sendMsgBtn;

    boolean toastShown = false; // Client Re-Connection
    long ACKEndTime;

    StopWatch stopWatch;

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
        EditMessageBox = (EditText) findViewById(R.id.messageBox);
        sendMsgBtn = (Button) findViewById(R.id.sendMsg);
        currentStatusText = (TextView) findViewById(R.id.currentStatus);
        peerConnectTime = (TextView) findViewById(R.id.pairingTime);
        speedText = (TextView) findViewById(R.id.speed);
        delayText = (TextView) findViewById(R.id.delay);
        delayMs = (TextView) findViewById(R.id.delayms);

        btStatusText.setSelected(true); // For Horizontal Scrolling
        messageReceived.setSelected(true); // For Horizontal Scrolling
        sendMsgBtn.setEnabled(false);

        btServerConnectionStatus = new Handler();
        btClientConnectionStatus = new Handler();
        bundle = new Bundle();

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        stopWatch = new StopWatch(delayText, delayMs);

        useFile = new FileServices(getApplicationContext());

        deviceConnected = false;
        retryConnectionHandler = new Handler();

        Dialog();
        startBluetooth();
        sendMessage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void Dialog(){
        new AlertDialog.Builder(this)
                .setTitle("Choose Server/Client")
                .setMessage("Do you want to connect as Server or a Client?")
                .setNegativeButton("Client", null)
                .setPositiveButton("Server", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        connectAsClient = false;
                    }
                }).create().show();
    }

    protected void startBluetooth() {
        String discMessage = "Discoverability set to ON";
        String btEnabledMessage = "Bluetooth is Enabled";
        Toast btDeviceDiscoverToast = Toast.makeText(getApplicationContext(), discMessage, Toast.LENGTH_SHORT);
        btDeviceDiscoverToast.show();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        getGoodOldName = mBluetoothAdapter.getName();

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

    private void setBtName() {
        String btDeviceName = "DTN-" + Build.SERIAL;
        String message = "Bluetooth Device Name: " + btDeviceName;
        mBluetoothAdapter.setName(btDeviceName);
        Toast btDeviceNameToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        btDeviceNameToast.show();
    }

    protected void discBluetoothDevices() {
        if (mBluetoothAdapter.isDiscovering()) {
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
                if (!device.equals(null)) {
                    btDevicesFoundList.add(device);
                }
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                String discBDevice = "Found Device: " + deviceName;
                noOfPeers++;
                Toast toast = Toast.makeText(getApplicationContext(), discBDevice, Toast.LENGTH_SHORT);
                toast.show();
                Log.i(Constants.TAG, "ACTION_FOUND is called! " + noOfPeers);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                btStatusText.setText("Discovery Period Finished");
                if(connectAsClient == false) {
                    serverConnection(); // Let's start the Server
                } else {
                    connectDevice();
                }
            } else if (btDeviceConnectedGlobal.ACTION_ACL_CONNECTED.equals(action)) {
                deviceConnected = true;
            }
            peerStatusText.setText("No of Peers Found: " + noOfPeers);
        }
    };

    public void connectDevice() {

        String btDeviceName = "DTN-";
        CLIENT_CONNECTION_FAIL = "Client Connection Failed!";

        btClientConnectionStatus = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(), CLIENT_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT);
                    toast.show();
                    currentStatusText.setText("CLIENT");
                    peerConnectTime.setText((long) msg.arg2 + " msec");

                    SocketGlobal = clientConnect.getClientSocket();
                    BandSocketGlobal = clientConnect.getBWClientSocket();
                    bandData = new BandwidthBytesT(BandSocketGlobal, btBandStatus);
                    streamData = new BluetoothBytesT(SocketGlobal, btMessageStatus, stopWatch);

                    speedText.setText("Calculating Bandwidth");

                    final Thread checkBandwidthT = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // Check Bandwidth
                            SystemClock.sleep(1000);
                            if(!useFile.checkFileExists(Constants.testFileName)) {
                            tempFile = useFile.createTemporaryFile(Constants.testFileName);
                            useFile.fillTempFile(tempFile);
                            } else {
                                tempFile = useFile.returnFile(Constants.testFileName);
                            }
                            bandData.checkBandwidth(useFile, tempFile);
                            FileSentBandwidth = (useFile.getFileSize() / bandData.getTotalBandwidthDuration());
                            Log.i(Constants.TAG, "From the thread after calculation:" + FileSentBandwidth);
                            // getDataHandler.sendEmptyMessage((int) FileSentBandwidth);
                            Log.i(Constants.TAG, "Check FileSentBandwidth From Thread:" + FileSentBandwidth);
                            Log.i(Constants.TAG, (String) (useFile.getFileSize() + " Time: " + bandData.getTotalBandwidthDuration()));
                            speedText.setText(String.format("%.2f", (FileSentBandwidth / 1024.0)) + " KBps");
                        }
                    });

                            checkBandwidthT.start();


                   //   getDataHandler = new Handler() {
                   //     @Override
                 //       public void handleMessage(Message msg) {
                         //   Log.i(Constants.TAG, "Check FileSentBandwidth:" + FileSentBandwidth);


                          /*  try {
                                checkBandwidthT.sleep(1000);
                                checkBandwidthT.run();
                            } catch (InterruptedException SleepE) {
                                Log.i(Constants.TAG, "checkBandwidthT is not able to sleep");
                            }

                        } */


                     //   }
                 //   };

                    streamData.start();
                    sendMsgBtn.setEnabled(true);


                } else if (msg.arg1 == -1) {
                    if (toastShown == false) {
                        Toast toast = Toast.makeText(getApplicationContext(), CLIENT_CONNECTION_FAIL, Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    if (deviceConnected == false) {
                        final Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                clientConnect.run(); // Keep Trying To Connect If It Fails.
                            }
                        };


                        retryConnectionHandler.postDelayed(r, 5000); // 5000 = 5 Secs. Does it do any good?

                    }


                } else if (msg.arg1 == 2) {
                        Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.ACK_CONNECT_CLIENT_SUCCESS, Toast.LENGTH_SHORT);
                        toast.show();

                    ACKSocketGlobal = clientConnect.getACKClientSocket();
                    ACKData = new BluetoothACKBytesT(ACKSocketGlobal, btACKStatus);
                    ACKData.start();
                }

                toastShown = true;
            }
        };

        for (BluetoothDevice btDevice : btDevicesFoundList) {
            Log.i(Constants.TAG, "BtDevicesfound " + btDevicesFoundList.equals(null));
            if (!btDevicesFoundList.equals(null)) {
                if ((btDevice.getName().contains(btDeviceName))) {
                    btDeviceConnectedGlobal = btDevice;
                    clientConnect = new BluetoothConnectClientT(btDevice, mBluetoothAdapter, btClientConnectionStatus);
                    clientConnect.start();
                }

            }
            if (!(btDeviceConnectedGlobal == null)) {
                CLIENT_CONNECTION_SUCCESSFUL = "Client Connected To:" + btDeviceConnectedGlobal.getName();
            } else {
                Log.e("DTN", "No Device Found With Name DTN");
            }
        }
    }

    private void serverConnection() {

        SERVER_CONNECTION_SUCCESSFUL = "Server is successfully connected!";
        SERVER_CONNECTION_FAIL = "Server failed to connect";

        btServerConnectionStatus = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(), SERVER_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT);
                    toast.show();
                    currentStatusText.setText("SERVER");
                    peerConnectTime.setText((long) msg.arg2 + " msec");

                    speedText.setVisibility(View.GONE);
                    sendMsgBtn.setEnabled(true);

                    SocketGlobal = serverConnect.getServerSocket();
                    streamData = new BluetoothBytesT(SocketGlobal, btMessageStatus, stopWatch);
                    streamData.start();
                } else if (msg.arg1 == -1) {
                    Toast toast = Toast.makeText(getApplicationContext(), SERVER_CONNECTION_FAIL, Toast.LENGTH_SHORT);
                    toast.show();
                } else if (msg.arg1 == 2) {
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.ACK_CONNECT_SERVER_SUCCESS, Toast.LENGTH_SHORT);
                    toast.show();

                    ACKSocketGlobal = serverConnect.getACKSocket();
                    ACKData = new BluetoothACKBytesT(ACKSocketGlobal, btACKStatus);
                    ACKData.start();
                } else if(msg.what == 3){
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.BW_CONNECT_SERVER_SUCCESS, Toast.LENGTH_SHORT);
                    toast.show();

                    BandSocketGlobal = serverConnect.getBWSocket();
                    bandData = new BandwidthBytesT(BandSocketGlobal, btBandStatus);
                    bandData.start();
                }
            }
        };


        serverConnect = new BluetoothConnectT(mBluetoothAdapter, btServerConnectionStatus);
        serverConnect.start();
    }

    private final Handler btMessageStatus = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MessageConstants.MESSAGE_WRITE) {
                btStatusText.setText("Message is sent");
            } else if (msg.what == Constants.MessageConstants.MESSAGE_TOAST) {
                String statusMessage = bundle.getString("status");
                btStatusText.setText(statusMessage);
            } else if ((msg.what == Constants.MessageConstants.MESSAGE_READ)) {
                btStatusText.setText("Message received");
                byte[] writeBuf = (byte[]) msg.obj;
                byte[] writeACK = new byte[] {'R'};
                String writeMessage = new String(writeBuf);
                // if(!isCheckingBandwidth) {
              //  Log.i(Constants.TAG, "Message Received: " + writeMessage);
                messageReceived.setText(writeMessage);
                // }
                ACKData.write(writeACK);
                // isCheckingBandwidth = false;
                Log.i(Constants.TAG, "Am I inside Message Received Handler? " + true);
            }
        }
    };

    private final Handler btACKStatus = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MessageConstants.ACK_READ) {
                byte[] writeBuf = (byte[]) msg.obj;
                Log.i(Constants.TAG, "I received writeBuf(ACK_READ): " + new String(writeBuf));
                if(writeBuf[0] == 'R') {
                    Log.i(Constants.TAG, "I am inside the if condition in ACK writeBuf");
                    stopWatch.halt();
                }
            } else if(msg.what == Constants.MessageConstants.ACK_WRITE) {
                Log.i(Constants.TAG, "I am sending an ACK.");
            }
        }
    };

    private final Handler btBandStatus = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == Constants.MessageConstants.BW_READ){
                byte[] writeBuf = (byte[]) msg.obj;
                Log.i(Constants.TAG, "BW Received: " + new String(writeBuf));
            } else if(msg.what ==  Constants.MessageConstants.BW_WRITE){
                // Do Nothing
            }
        }
    };

    public void sendMessage() {

        NOT_YET_CONNECTED = "I am not yet connected to any phone";

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(SocketGlobal == null)) {
                    streamData.writePackets((EditMessageBox.getText().toString()).getBytes());
                    Log.i(Constants.TAG, "Message Sent: " + EditMessageBox.getText());
                    streamData.flushOutStream();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), NOT_YET_CONNECTED, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        });
    }


    @Override
    protected void onDestroy() {
        mBluetoothAdapter.setName(getGoodOldName);
        mBluetoothAdapter.disable();
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }


}
