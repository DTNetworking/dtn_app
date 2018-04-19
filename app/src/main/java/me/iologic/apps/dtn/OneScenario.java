package me.iologic.apps.dtn;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OneScenario extends AppCompatActivity {


    BluetoothAdapter mBluetoothAdapter; // The Only Bluetooth Adapter Used.
    boolean connectAsClient = true;
    int noOfPeers = 0;

    BluetoothConnectSmmSocket serverMessageSConnect;
    BluetoothConnectSBWSocket serverBWConnect;
    BluetoothConnectSACKSocket serverACKConnect;
    BluetoothConnectSsecondmmSocket serverSecondMessageSConnect;
    BluetoothConnectSsecondBWSocket serverSecondBWConnect;
    BluetoothConnectSsecondACKSocket serverSecondACKConnect;

    BluetoothBytesT streamData;
    BandwidthBytesT bandData;
    BluetoothACKBytesT ACKData;
    BluetoothDevice btDeviceConnectedGlobal; // To get Device Name
    BluetoothSocket SocketGlobal; // To store MAIN socket
    BluetoothSocket ACKSocketGlobal; // To store ACK socket
    BluetoothSocket BandSocketGlobal; // To store Bandwidth Socket
    BluetoothSocket secondSocketGlobal; // To store MAIN socket
    BluetoothSocket secondACKSocketGlobal; // To store ACK socket
    BluetoothSocket secondBandSocketGlobal; // To store Bandwidth Socket
    ArrayList<BluetoothDevice> btDevicesFoundList = new ArrayList<BluetoothDevice>(); // Store list of bluetooth devices.
    ArrayList<ContactTimeList> contactTimeList = new ArrayList<>();
    ArrayList<BluetoothDevice> btDeviceConnectedList = new ArrayList<BluetoothDevice>();
    String getGoodOldName;

    // 2nd Connection
    SecondBluetoothBytesT streamSecondData;
    SecondBluetoothACKBytesT secondACKData;
    SecondBandwidthBytesT secondBandData;

    String saveFileUUID;

    AlertDialog alertDialog;
    boolean alertDialogOpened;

    Handler btServerConnectionStatus;
    Bundle bundle;

    FileServices useFile;
    File tempFile;

    double FileSentBandwidth;
    Handler getDataHandler;
    Handler progressBarHandler;
    Handler writeBWPacketLossHandler;
    boolean deviceConnected;
    Handler retryConnectionHandler = new Handler();
    String GlobalReceivedMessage;
    String globalBandwidth;
    boolean BWStart, BWPacketLossCheckStart;
    double GlobalMsgPacketLoss;
    double GlobalBWPacketLoss;
    boolean isFirstPhoneConnected;

    private static String NOT_YET_CONNECTED;

    TextView btStatusText;
    TextView peerStatusText;
    TextView messageReceived;
    TextView currentStatusText;
    TextView currentStatusConText;
    TextView currentStatusSecPhoneText;
    TextView currentStatusSecConnectedText;
    TextView peerConnectTime;
    TextView bandwidthText;
    TextView delayText;
    TextView checkBandwidthText;
    EditText EditMessageBox;
    Button sendMsgBtn;
    TextView MsgPacketLossText;
    TextView BWPacketLossText;
    ProgressBar sendBWProgressBarView;
    TextView speedText;
    AVLoadingIndicatorView aviView;
    View divView;

    Animation animCrossFadeIn, animCrossFadeOut, animBlink;

    boolean toastShown = false; // Client Re-Connection
    long ACKEndTime;

    long connection1StartTime, connection1EndTime, duration;
    long interConnectTime;
    String interConnectTimeTxt;
    String currentDateTime, connectedDeviceName;

    long connection_two_StartTime, connection_two_EndTime, duration_two;
    long interConnectTime_two;
    String interConnectTimeTxt_two;
    String currentDateTime_two, connectedDeviceName_two;

    StopWatch stopWatch;

    DecimalFormat df;

    int packetReceivedCount;

    LightningMcQueen speed;
    double currentspeed;
    Indicators btFindIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_scenario);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btStatusText = (TextView) findViewById(R.id.btStatus);
        peerStatusText = (TextView) findViewById(R.id.peerStatus);
        messageReceived = (TextView) findViewById(R.id.messageStatus);
        EditMessageBox = (EditText) findViewById(R.id.messageBox);
        sendMsgBtn = (Button) findViewById(R.id.sendMsg);
        currentStatusText = (TextView) findViewById(R.id.currentStatus);
        currentStatusConText = (TextView) findViewById(R.id.currentStatusConnected);
        currentStatusSecPhoneText = (TextView) findViewById(R.id.currentStatusForSecondCon);
        currentStatusSecConnectedText = (TextView) findViewById(R.id.currentStatusForSecondConnected);
        peerConnectTime = (TextView) findViewById(R.id.pairingTime);
        bandwidthText = (TextView) findViewById(R.id.bandwidth);
        delayText = (TextView) findViewById(R.id.delay);
        checkBandwidthText = (TextView) findViewById(R.id.checkBandwidthStatus);
        MsgPacketLossText = (TextView) findViewById(R.id.MsgPacketLoss);
        BWPacketLossText = (TextView) findViewById(R.id.BWPacketLoss);
        sendBWProgressBarView = (ProgressBar) findViewById(R.id.sendBWProgressBar);
        speedText = (TextView) findViewById(R.id.speed);
        aviView = (AVLoadingIndicatorView) findViewById(R.id.avi);
        divView = (View) findViewById(R.id.divider);

        checkBandwidthText.setVisibility(View.GONE);
        BWPacketLossText.setVisibility(View.GONE);

        btStatusText.setSelected(true); // For Horizontal Scrolling
        messageReceived.setSelected(true); // For Horizontal Scrolling
        sendMsgBtn.setEnabled(false);

        animCrossFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        animCrossFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);
        animBlink = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.blink);

        btServerConnectionStatus = new Handler();
        bundle = new Bundle();

        saveFileUUID = UUID.randomUUID().toString();

        useFile = new FileServices(getApplicationContext(), saveFileUUID);
        saveFileUUID = UUID.randomUUID().toString();

        useFile = new FileServices(getApplicationContext(), saveFileUUID);

        speed = new LightningMcQueen();

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                currentspeed = speed.getSpeed(location);
                String showSpeed = currentspeed + " m/s";
                speedText.setText(showSpeed);
                // useFile.saveSpeedData(Constants.FileNames.LightningMcQueen, showSpeed);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        };

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            Log.e(Constants.TAG, "Location Error:" + e);
        }

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        stopWatch = new StopWatch(delayText);

        deviceConnected = false;
        retryConnectionHandler = new Handler();

        alertDialogOpened = false;

        BWStart = true;
        BWPacketLossCheckStart = true;

        df = new DecimalFormat("#.00");
        packetReceivedCount = 0;

        btFindIndicator = new Indicators();

        writeBandwidthLossData();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle Item Selection
        switch (item.getItemId()) {
            case R.id.action_msgTime:
                showMsgTimeList();
                return true;
            case R.id.action_intercontactTime:
                Intent interContactTimeIntent = new Intent(this, ContactTimeListView.class);
                interContactTimeIntent.putExtra("contactTimeListArray", contactTimeList);
                startActivity(interContactTimeIntent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showContactTimeList() {
        alertDialogOpened = true;
        AlertDialog.Builder showContactTimeList = new AlertDialog.Builder(OneScenario.this);
        showContactTimeList.setTitle("Inter Contact Timings");
    }

    public void showMsgTimeList() {
        alertDialogOpened = true;
        AlertDialog.Builder showList = new AlertDialog.Builder(OneScenario.this);
        showList.setTitle("Message Timings");

        String[] msgTimings = new String[stopWatch.getTimings().size()];
        msgTimings = stopWatch.getTimings().toArray(msgTimings);

        showList.setItems(msgTimings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do Nothing
            }
        });

        alertDialog = showList.create();
        try {
            alertDialog.show();
        } catch (Exception e) {
            alertDialog.dismiss();
        }
    }

    public void Dialog() {
        new AlertDialog.Builder(this)
                .setTitle("Choose Server/Client")
                .setMessage("Do you want to connect as Server or a Client?")
                .setNegativeButton("Client", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Do Nothing
                    }
                })
                .setPositiveButton("Server", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        connectAsClient = false;
                    }
                }).create().show();
    }

    public void startIndicator() {
        // Start btFind Indicator
        aviView.setVisibility(View.VISIBLE);
        btFindIndicator.startAnim(aviView);
    }

    public void stopIndicator() {
        // Start btFind Indicator
        aviView.setVisibility(View.GONE);
        btFindIndicator.stopAnim(aviView);
    }

    protected void startBluetooth() {
        String btEnabledMessage = "Bluetooth is Enabled";

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        getGoodOldName = mBluetoothAdapter.getName(); // For replacing name when Activity Exits

        if (mBluetoothAdapter == null) {
            btStatusText.setText("Bluetooth Not Found!");
        } else if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            //   Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            // startActivityForResult(enableBtIntent, Constants.Permissions.REQUEST_ENABLE_BT); // Calls onActivityResult */

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
            startIndicator();

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
                if ((device != null) && (device.getName() != null) && (device.getName() != "null")) {
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
                if (connectAsClient == false) {
                    serverConnection(); // Let's start the Server
                } else {
                    // connectDevice();
                }
            } else if (btDeviceConnectedGlobal.ACTION_ACL_CONNECTED.equals(action)) {
                deviceConnected = true;
                btDeviceConnectedList.add(btDeviceConnectedGlobal);
            } else if (btDeviceConnectedList.get(0).ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.e(Constants.TAG, "ONE DEVICE IS DISCONNECTED!");
                connection1EndTime = System.nanoTime();
                duration = connection1EndTime - connection1StartTime;
                long durationInSeconds = TimeUnit.NANOSECONDS.toSeconds(duration);
                if (durationInSeconds < 60) {
                    interConnectTime = durationInSeconds;
                    interConnectTimeTxt = interConnectTime + " seconds";
                } else {
                    interConnectTime = TimeUnit.SECONDS.toMinutes(durationInSeconds);
                    interConnectTimeTxt = interConnectTime + " minutes";
                }

                //list
                ContactTimeList device1 = new ContactTimeList(btDeviceConnectedList.get(0).getName(), currentDateTime, interConnectTimeTxt);
                contactTimeList.add(device1);
                try {
                    useFile.saveInterContactTime(Constants.FileNames.InterContactTime, btDeviceConnectedList.get(0).getName(), currentDateTime, interConnectTimeTxt);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), ("Device " + btDeviceConnectedList.get(0).getName() + " is disconnected!"), Toast.LENGTH_SHORT).show();
            } else if (btDeviceConnectedList.get(1).ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.e(Constants.TAG, "SECOND DEVICE IS DISCONNECTED!");
                connection_two_EndTime = System.nanoTime();
                duration = connection_two_EndTime - connection_two_StartTime;
                long durationInSeconds_two = TimeUnit.NANOSECONDS.toSeconds(duration);
                if (durationInSeconds_two < 60) {
                    interConnectTime_two = durationInSeconds_two;
                    interConnectTimeTxt_two = interConnectTime_two + " seconds";
                } else {
                    interConnectTime_two = TimeUnit.SECONDS.toMinutes(durationInSeconds_two);
                    interConnectTimeTxt_two = interConnectTime_two + " minutes";
                }

                //list
                ContactTimeList device2 = new ContactTimeList(btDeviceConnectedList.get(1).getName(), currentDateTime_two, interConnectTimeTxt_two);
                contactTimeList.add(device2);
                try {
                    useFile.saveInterContactTime(Constants.FileNames.InterContactTime, btDeviceConnectedList.get(1).getName(), currentDateTime_two, interConnectTimeTxt_two);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), ("Device " + btDeviceConnectedList.get(1).getName() + " is disconnected!"), Toast.LENGTH_SHORT).show();
            }

            peerStatusText.setText("No of Peers Found: " + noOfPeers);
        }
    };

    private void serverConnection() {

        btServerConnectionStatus = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.SERVER_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT);
                    toast.show();
                    currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    connection1StartTime = System.nanoTime();
                    stopIndicator();
                    isFirstPhoneConnected = true;
                    currentStatusConText.setText(R.string.server);
                    currentStatusConText.setVisibility(View.VISIBLE);
                    // start fade in animation
                    currentStatusConText.startAnimation(animCrossFadeIn);

                    // start fade out animation
                    currentStatusText.startAnimation(animCrossFadeOut);

                    peerConnectTime.setText((long) msg.arg2 + " msec");
                    useFile.savePairingData(Constants.FileNames.Pairing, "CLIENT", msg.arg2);
                    bandwidthText.setVisibility(View.GONE);
                    sendBWProgressBarView.setVisibility(View.GONE);
                    BWPacketLossText.setVisibility(View.GONE);
                    sendMsgBtn.setEnabled(true);

                    SocketGlobal = serverMessageSConnect.getServerSocket();
                    streamData = new BluetoothBytesT(SocketGlobal, btMessageStatus, stopWatch);
                    streamData.start();

                } else if (msg.arg1 == -1) {
                    aviView.setIndicatorColor(Color.RED);
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.SERVER_CONNECTION_FAIL, Toast.LENGTH_SHORT);
                    toast.show();
                } else if (msg.arg1 == 2) {
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.ACK_CONNECT_SERVER_SUCCESS, Toast.LENGTH_SHORT);
                    toast.show();

                    ACKSocketGlobal = serverACKConnect.getACKSocket();
                    ACKData = new BluetoothACKBytesT(ACKSocketGlobal, btACKStatus);
                    ACKData.start();

                } else if (msg.arg1 == 3) {
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.BW_CONNECT_SERVER_SUCCESS, Toast.LENGTH_SHORT);
                    toast.show();

                    BandSocketGlobal = serverBWConnect.getBWSocket();
                    bandData = new BandwidthBytesT(BandSocketGlobal, btBandStatus);
                    bandData.start();
                }

                //for 2nd connection
                else if (msg.arg1 == 8) {

                    Toast.makeText(getApplicationContext(), Constants.MessageConstants.SECOND_SERVER_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT).show();
                    currentDateTime_two = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
                    connection_two_StartTime = System.nanoTime();

                    currentStatusSecConnectedText.setText(R.string.server);

                    currentStatusSecConnectedText.setVisibility(View.VISIBLE);
                    // start fade in animation
                    currentStatusSecConnectedText.startAnimation(animCrossFadeIn);

                    // start fade out animation
                    currentStatusSecPhoneText.startAnimation(animCrossFadeOut);

                    currentStatusSecPhoneText.setVisibility(View.GONE);

                    Thread changeDividerColorT = new Thread() {
                        @Override
                        public void run() {
                            while (true) {
                                if (isFirstPhoneConnected) {
                                    currentStatusText.setVisibility(View.GONE);
                                    divView.setBackgroundColor(Color.parseColor("#FF00FF00"));
                                }
                            }
                        }
                    };

                    changeDividerColorT.start();

                    // 2nd connection
                    secondSocketGlobal = serverSecondMessageSConnect.getServerSocket();
                    streamSecondData = new SecondBluetoothBytesT(secondSocketGlobal, btMessageStatus);
                    Log.i(Constants.TAG, "Second Connection Started!");
                    streamSecondData.start();


                } else if (msg.arg1 == 9) {
                    Toast.makeText(getApplicationContext(), Constants.MessageConstants.SECOND_ACK_CONNECT_SERVER_SUCCESS, Toast.LENGTH_SHORT).show();

                    // 2nd Connection
                    secondACKSocketGlobal = serverSecondACKConnect.getACKSocket();
                    secondACKData = new SecondBluetoothACKBytesT(secondACKSocketGlobal, btACKStatus);
                    secondACKData.start();

                } else if (msg.arg1 == 10) {
                    Toast.makeText(getApplicationContext(), Constants.MessageConstants.SECOND_BW_CONNECT_SERVER_SUCCESS, Toast.LENGTH_SHORT).show();

                    secondBandSocketGlobal = serverSecondBWConnect.getBWSocket();
                    secondBandData = new SecondBandwidthBytesT(secondBandSocketGlobal, btBandStatus);
                    secondBandData.start();

                }
            }
        };

        serverMessageSConnect = new BluetoothConnectSmmSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverMessageSConnect.start();

        serverACKConnect = new BluetoothConnectSACKSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverACKConnect.start();

        serverBWConnect = new BluetoothConnectSBWSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverBWConnect.start();

        serverSecondMessageSConnect = new BluetoothConnectSsecondmmSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverSecondMessageSConnect.start();

        serverSecondACKConnect = new BluetoothConnectSsecondACKSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverSecondACKConnect.start();

        serverSecondBWConnect = new BluetoothConnectSsecondBWSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverSecondBWConnect.start();
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
                // byte[] writeACK = new byte[]{'R'};
                // String writeACK = String.valueOf(msg.arg1);
                String writeMessage = new String(writeBuf);
                // if(!isCheckingBandwidth) {
                //  Log.i(Constants.TAG, "Message Received: " + writeMessage);
                messageReceived.setText(writeMessage);
                // }
                GlobalReceivedMessage = writeMessage;
                // ACKData.write(writeACK.getBytes());
                // isCheckingBandwidth = false;
                String showSpeed = currentspeed + " m/s";
                useFile.saveSpeedData(Constants.FileNames.Speed, showSpeed);
                Log.i(Constants.TAG, "Am I inside Message Received Handler? " + true);
                writeForSecondConnection(writeMessage);
            }
        }
    };

    private final Handler btACKStatus = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MessageConstants.ACK_READ) {
                byte[] writeBuf = (byte[]) msg.obj;
                Log.i(Constants.TAG, "I received writeBuf(ACK_READ): " + new String(writeBuf));
                if (writeBuf[0] == 'R') {
                    Log.i(Constants.TAG, "I am inside the if condition in ACK writeBuf");
                    //stopWatch.halt();
                    // Update Message Timing List and Reset The Timer
                    //useFile.saveDelayData(Constants.FileNames.Delay, stopWatch.getGlobalTime());
                    // stopWatch.updateList();
                    // stopWatch.reset();
                    // byte[] writeACK = new byte[]{'R'};
                    writeACKForSecondConnection(writeBuf);
                } else {
                    Log.i(Constants.TAG, "I am inside the else condition in ACK writeBuf");
                    stopWatch.halt();
                    // Update Message Timing List and Reset The Timer
                    useFile.saveDelayData(Constants.FileNames.Delay, stopWatch.getGlobalTime());
                    stopWatch.updateList();
                    stopWatch.reset();
                    writeACKForSecondConnection(writeBuf);
                }
                GlobalMsgPacketLoss = streamData.getPacketLoss(EditMessageBox.getText().length(), new String(writeBuf)); // For 1st Scenario
                String showMsgLossPercent = df.format(GlobalMsgPacketLoss) + "%";
                if (GlobalMsgPacketLoss == 0) {
                    MsgPacketLossText.setTextColor(Color.GRAY);
                    MsgPacketLossText.setText("0" + showMsgLossPercent);
                } else {
                    MsgPacketLossText.setTextColor(Color.RED);
                    MsgPacketLossText.setText(showMsgLossPercent);
                }

                useFile.savePacketLossData(Constants.FileNames.MsgPacketLoss, GlobalMsgPacketLoss);

            } else if (msg.what == Constants.MessageConstants.ACK_WRITE)

            {
                Log.i(Constants.TAG, "I am sending an ACK -> " + GlobalReceivedMessage);
                Log.i(Constants.TAG, "---------------------");
            }
        }
    };

    private final Handler btBandStatus = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MessageConstants.BW_READ) {
                // byte[] writeBuf = (byte[]) msg.obj;
                // Log.i(Constants.TAG, "BW Received: " + new String(writeBuf));
                // Log.i(Constants.TAG, "BW Size: " + writeBuf.length);
                // byte[] writeBuf = (byte[]) msg.obj;
                // Log.i(Constants.TAG, "BW Received: " + new String(writeBuf));
                // Log.i(Constants.TAG, "BW Size: " + writeBuf.length);
            } else if (msg.what == Constants.MessageConstants.BW_WRITE) {
                // Do Nothing
                checkBandwidthText.setTextColor(Color.parseColor("#566680"));
                FileSentBandwidth = ((double) Constants.Packet.BW_PACKET_SIZE / bandData.getTotalBandwidthDuration());
                // Log.i(Constants.TAG, "Bandwidth Duration: " + bandData.getTotalBandwidthDuration());
                // Log.i(Constants.TAG, "Check FileSentBandwidth:" + FileSentBandwidth);
                String bandwidth = String.format("%.2f", (FileSentBandwidth / 1024.0)) + " KBps";
                globalBandwidth = bandwidth;
                bandwidthText.setText(bandwidth);
                getDataHandler.sendEmptyMessage((int) FileSentBandwidth); // Send anything
                progressBarHandler.sendEmptyMessage(msg.arg1);
                checkBandwidthText.setText("No. Of Bandwidth Packets Sent: " + msg.arg1);
            } else if (msg.what == Constants.MessageConstants.BW_START_WRITE) {
                final Thread writeBandwidthToFileT = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        getDataHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                useFile.saveBWData(Constants.FileNames.Bandwidth, globalBandwidth);
                            }
                        };
                        Looper.loop();
                    }

                });

                final Thread sendBWProgressBarT = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        progressBarHandler = new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                sendBWProgressBarView.setProgress(msg.what);
                            }
                        };
                        Looper.loop();
                    }
                });

                if (msg.arg1 == 1 && BWStart) {
                    BWStart = false;
                    checkBandwidthText.setText(R.string.checkingBandwidth);
                    writeBandwidthToFileT.start();
                    sendBWProgressBarView.setVisibility(View.VISIBLE);
                    sendBWProgressBarT.start();
                }
            } else if (msg.what == Constants.MessageConstants.BW_PACKET_LOSS_CHECK) {
                double packetLost = ((double) (Constants.Packet.BW_COUNTER - msg.arg1) / (double) (Constants.Packet.BW_COUNTER)) * 100;
                GlobalBWPacketLoss = packetLost;
                writeBWPacketLossHandler.sendEmptyMessage((int) GlobalBWPacketLoss); // Send Anything
                String BWLossPercent = df.format(GlobalBWPacketLoss) + " %";

                if (msg.arg1 != 16) {
                    //Log.i(Constants.TAG, "msg.arg1: " + msg.arg1 + " BWLossPercent: " + BWLossPercent);
                    BWPacketLossText.setTextColor(Color.RED);
                    BWPacketLossText.setText(BWLossPercent);
                } else {
                    BWPacketLossText.setTextColor(Color.GRAY);
                    BWPacketLossText.setText("0" + BWLossPercent);
                }
            }
        }
    };

    public void writeBandwidthLossData() {
        final Thread writeGlobalPacketLossT = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                writeBWPacketLossHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        useFile.savePacketLossData(Constants.FileNames.BWPacketLoss, GlobalBWPacketLoss);
                    }
                };
                Looper.loop();
            }
        });

        if (BWPacketLossCheckStart) {
            BWPacketLossCheckStart = false;
            writeGlobalPacketLossT.start();
        }
    }

    public void sendMessage() {

        NOT_YET_CONNECTED = "I am not yet connected to any phone";

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(SocketGlobal == null)) {
                    String temp = EditMessageBox.getText() + "_";
                    streamData.writePackets(temp.getBytes());
                    Log.i(Constants.TAG, "Message Sent: " + EditMessageBox.getText());
                    streamData.flushOutStream();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), NOT_YET_CONNECTED, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        });
    }

    public void writeForSecondConnection(String ReceivedString) {

        NOT_YET_CONNECTED = "I am not yet connected to any phone";

        byte[] sendBytes = ReceivedString.substring(0, 10).getBytes();

        if (!(SocketGlobal == null) && !(secondSocketGlobal == null)) {
            streamSecondData.write(sendBytes);
            Toast.makeText(getApplicationContext(), "Message Sent To 3rd Phone: " + ReceivedString, Toast.LENGTH_SHORT).show();
            Log.i(Constants.TAG, "Message Sent To 3rd Phone: " + ReceivedString);
            streamSecondData.flushOutStream();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), NOT_YET_CONNECTED, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void writeACKForSecondConnection(byte[] ReceivedByte) {
        ACKData.write(ReceivedByte);
        Toast.makeText(getApplicationContext(), "I sent the ACK which I received from the 3rd Phone!", Toast.LENGTH_SHORT);
    }


    @Override
    protected void onDestroy() {
        mBluetoothAdapter.setName(getGoodOldName);
        mBluetoothAdapter.disable();
        if (alertDialogOpened == true) {
            alertDialog.dismiss();
        }
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }


}
