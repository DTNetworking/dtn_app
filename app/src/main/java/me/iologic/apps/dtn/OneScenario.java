package me.iologic.apps.dtn;

import android.app.Activity;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sunfusheng.marqueeview.MarqueeView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static me.iologic.apps.dtn.Constants.Permissions.READ_REQUEST_CODE;

public class OneScenario extends AppCompatActivity {


    BluetoothAdapter mBluetoothAdapter; // The Only Bluetooth Adapter Used.
    boolean connectAsClient = true;
    int noOfPeers = 0;
    String connectedDeviceName;

    BluetoothConnectSmmSocket serverMessageSConnect;
    BluetoothConnectCmmSocket clientMessageSConnect;

    BluetoothConnectSACKSocket serverACKConnect;
    BluetoothConnectCACKSocket clientACKConnect;

    BluetoothConnectSBWSocket serverBWConnect;

    BluetoothConnectCBWSocket clientBWConnect;

    UUIDManager deviceUUIDs;


    BluetoothBytesT streamData;
    BandwidthBytesT bandData;
    BluetoothACKBytesT ACKData;
    BluetoothDevice btDeviceConnectedGlobal; // To get Device Name
    BluetoothSocket SocketGlobal; // To store MAIN socket
    BluetoothSocket ACKSocketGlobal; // To store ACK socket
    BluetoothSocket BandSocketGlobal; // To store Bandwidth Socket
    ArrayList<BluetoothDevice> btDevicesFoundList = new ArrayList<BluetoothDevice>(); // Store list of bluetooth devices.
    ArrayList<ContactTimeList> contactTimeList = new ArrayList<>();
    String getGoodOldName;
    String currentDateTime;
    String saveFileUUID;

    String deviceType;

    String fileTypeStatus; // To check type of file being received.
    int sentDataSize;

    AlertDialog alertDialog;
    boolean alertDialogOpened;

    Handler btClientConnectionStatus;
    Handler btServerConnectionStatus;
    Bundle bundle;

    FileServices useFile;
    File tempFile;
    ImageData img;

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

    long connection1StartTime, connection1EndTime, duration;

    Uri ImageUri;


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
    TextView interContactTimeTxTView;
    TextView bandwidthText;
    TextView delayText;
    TextView checkBandwidthText;
    EditText EditMessageBox;
    ImageButton sendMsgBtn;
    ImageButton sendImgBtn;
    TextView MsgPacketLossText;
    TextView BWPacketLossText;
    ProgressBar sendBWProgressBarView;
    TextView speedText;
    TextView bytesReceivedText;
    TextView bytesSentText;
    AVLoadingIndicatorView aviView;
    TextView charLimitTxtView;

    LinearLayout dataselLLayout;
    LinearLayout BWProgressLLayout;

    boolean toastShown = false; // Client Re-Connection
    long ACKEndTime;

    StopWatch stopWatch;

    DecimalFormat df;

    long interConnectTime;
    String interConnectTimeTxt;

    int packetReceivedCount;

    LightningMcQueen speed;
    double currentspeed;
    Indicators btFindIndicator;

    Animation animFadeIn, animFadeOut, animSlideOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_scenario);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        btStatusText = (TextView) findViewById(R.id.btStatus);
        peerStatusText = (TextView) findViewById(R.id.peerStatus);
        messageReceived = (TextView) findViewById(R.id.messageStatus);
        EditMessageBox = (EditText) findViewById(R.id.messageBox);
        sendMsgBtn = (ImageButton) findViewById(R.id.sendMsg);
        sendImgBtn = (ImageButton) findViewById(R.id.sendImg);
        currentStatusText = (TextView) findViewById(R.id.currentStatus);
        peerConnectTime = (TextView) findViewById(R.id.pairingTime);
        interContactTimeTxTView = (TextView) findViewById(R.id.interContactTime);
        bandwidthText = (TextView) findViewById(R.id.bandwidth);
        delayText = (TextView) findViewById(R.id.delay);
        checkBandwidthText = (TextView) findViewById(R.id.checkBandwidthStatus);
        MsgPacketLossText = (TextView) findViewById(R.id.MsgPacketLoss);
        BWPacketLossText = (TextView) findViewById(R.id.BWPacketLoss);
        sendBWProgressBarView = (ProgressBar) findViewById(R.id.sendBWProgressBar);
        speedText = (TextView) findViewById(R.id.speed);
        bytesSentText = (TextView) findViewById(R.id.bytesSent);
        bytesReceivedText = (TextView) findViewById(R.id.bytesReceived);
        aviView = (AVLoadingIndicatorView) findViewById(R.id.avi);
        charLimitTxtView = (TextView) findViewById(R.id.characterLimitTxt);

        dataselLLayout = (LinearLayout) findViewById(R.id.dataSelLinearLayout);
        BWProgressLLayout = (LinearLayout) findViewById(R.id.BWProgressLL);

        checkBandwidthText.setVisibility(View.GONE);
        BWPacketLossText.setVisibility(View.GONE);

        btStatusText.setSelected(true); // For Horizontal Scrolling
        messageReceived.setSelected(true); // For Horizontal Scrolling
        sendMsgBtn.setEnabled(false);
        sendImgBtn.setEnabled(false);

        btServerConnectionStatus = new Handler();
        btClientConnectionStatus = new Handler();
        bundle = new Bundle();

        saveFileUUID = UUID.randomUUID().toString();

        useFile = new FileServices(getApplicationContext(), saveFileUUID);
        saveFileUUID = UUID.randomUUID().toString();

        useFile = new FileServices(getApplicationContext(), saveFileUUID);

        speed = new LightningMcQueen();

        img = new ImageData();

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
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mReceiver, filter);

        fileTypeStatus = Constants.DataTypes.TEXT; // By default let it be "Text"

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

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);
        animSlideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out);


        startBluetooth();
        DeviceType();
        sendMessage();
        sendImage();
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
            case R.id.action_showImage:
                Intent intent = new Intent(this, ImageViewer.class);
                startActivity(intent);
                return true;
            case R.id.action_connectionReconnect:
                if (streamData != null) {
                    Toast.makeText(getApplicationContext(), Constants.EmulationMessages.CLIENTCONNECT_GETTING_DISCONNECTED, Toast.LENGTH_SHORT).show();
                    clientMessageSConnect.cancel();
                } else {
                    Toast.makeText(getApplicationContext(), Constants.EmulationMessages.CLIENTCONNECT_NOT_CONNECTED, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_intercontactTime:
                Intent interContactTimeIntent = new Intent(this, ContactTimeListView.class);
                interContactTimeIntent.putExtra("contactTimeListArray", contactTimeList);
                startActivity(interContactTimeIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public void DeviceType() {
        if (mBluetoothAdapter.getName().equals(Constants.DeviceNames.originDevice)) {
            connectAsClient = false;
        } else if (mBluetoothAdapter.getName().equals(Constants.DeviceNames.destinationDevice)) {
            connectAsClient = true;
        }
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

        deviceUUIDs = new UUIDManager(mBluetoothAdapter.getName());

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
                peerStatusText.startAnimation(animSlideOut);
                peerStatusText.setVisibility(View.GONE);
                getUUIDs(); // Check if UUIDs are discoverable
                if (connectAsClient == false) {
                    serverConnection(); // Let's start the Server
                } else {
                    connectDevice();
                }
            } else if (btDeviceConnectedGlobal.ACTION_ACL_CONNECTED.equals(action)) {
                deviceConnected = true;
            } else if (btDeviceConnectedGlobal.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.e(Constants.TAG, "DEVICE IS DISCONNECTED!");
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
                ContactTimeList device1 = new ContactTimeList(connectedDeviceName, currentDateTime, interConnectTimeTxt);
                contactTimeList.add(device1);
                useFile.saveInterContactTime(Constants.FileNames.InterContactTime, connectedDeviceName, currentDateTime, interConnectTimeTxt);
                Toast.makeText(getApplicationContext(), ("Device " + connectedDeviceName + " is disconnected!"), Toast.LENGTH_SHORT).show();
            }

            peerStatusText.setText("No of Peers Found: " + noOfPeers);
        }

    };

    public void connectDevice() {

        String btDeviceName = Constants.DeviceNames.thirdRouterDevice;

        if (mBluetoothAdapter.getName().equals(Constants.DeviceNames.originDevice)) {
            btDeviceName = Constants.DeviceNames.secondRouterDevice;
        } else if (mBluetoothAdapter.getName().equals(Constants.DeviceNames.destinationDevice)) {
            btDeviceName = Constants.DeviceNames.originDevice;
        }

        btClientConnectionStatus = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(), CLIENT_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT);
                    toast.show();
                    stopIndicator();
                    currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());

                    deviceType = Constants.DeviceTypes.CLIENT;
                    connection1StartTime = System.nanoTime();
                    currentStatusText.startAnimation(animFadeIn);
                    currentStatusText.setText(Constants.DeviceTypes.CLIENT);
                    peerConnectTime.setText((long) msg.arg2 + " msec");
                    useFile.savePairingData(Constants.FileNames.Pairing, Constants.DeviceTypes.CLIENT, msg.arg2);
                    SocketGlobal = clientMessageSConnect.getClientSocket();
                    streamData = new BluetoothBytesT(SocketGlobal, btMessageStatus, stopWatch);

                    streamData.start();
                    sendMsgBtn.setEnabled(true);
                    sendImgBtn.setEnabled(true);

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            checkBandwidth();
                        }
                    }, 2000);


                } else if (msg.arg1 == -1) {
                    if (toastShown == false) {
                        aviView.setIndicatorColor(Color.MAGENTA);
                        Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.CLIENT_CONNECTION_FAIL, Toast.LENGTH_SHORT);
                        toast.show();

                    }

                } else if (msg.arg1 == 2) {
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.ACK_CONNECT_CLIENT_SUCCESS, Toast.LENGTH_SHORT);
                    toast.show();

                    ACKSocketGlobal = clientACKConnect.getACKClientSocket();
                    ACKData = new BluetoothACKBytesT(ACKSocketGlobal, btACKStatus);
                    ACKData.start();
                } else if (msg.arg1 == 100) {
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.BW_CONNECT_CLIENT_SUCCESS, Toast.LENGTH_SHORT);
                    toast.show();

                    BandSocketGlobal = clientBWConnect.getBWClientSocket();
                    bandData = new BandwidthBytesT(BandSocketGlobal, btBandStatus);
                    bandData.start();


                } else if (msg.arg1 == 200) {
                    // Toast.makeText(getApplicationContext(), "Yes I am disconnected", Toast.LENGTH_LONG).show();
                }

                toastShown = true;
            }
        };

        for (BluetoothDevice btDevice : btDevicesFoundList) {
            Log.i(Constants.TAG, "BtNullDevicefound " + btDevice.equals(null));
            if (!(btDevice.equals(null) && !(btDevice.getName().equals("null")) && !(btDevice.getName().equals(null)))) {
                if ((btDevice.getName().contains(btDeviceName))) {
                    btDeviceConnectedGlobal = btDevice;

                    clientBWConnect = new BluetoothConnectCBWSocket(btDevice, btClientConnectionStatus, deviceUUIDs);
                    clientBWConnect.start();

                    clientMessageSConnect = new BluetoothConnectCmmSocket(btDevice, btClientConnectionStatus, deviceUUIDs);
                    clientMessageSConnect.start();

                    clientACKConnect = new BluetoothConnectCACKSocket(btDevice, btClientConnectionStatus, deviceUUIDs);
                    clientACKConnect.start();

                }

            }
            if (!(btDeviceConnectedGlobal == null)) {
                CLIENT_CONNECTION_SUCCESSFUL = "Client Connected To:" + btDeviceConnectedGlobal.getName();
                connectedDeviceName = btDeviceConnectedGlobal.getName();
            } else {
                aviView.setIndicatorColor(Color.DKGRAY);
                Log.e("DTN", "No Device Found With Name DTN");
            }
        }
    }

    ;

    private void serverConnection() {

        btServerConnectionStatus = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.arg1 == 1) {
                    Toast toast = Toast.makeText(getApplicationContext(), Constants.MessageConstants.SERVER_CONNECTION_SUCCESSFUL, Toast.LENGTH_SHORT);
                    toast.show();
                    stopIndicator();
                    deviceType = Constants.DeviceTypes.SERVER;
                    currentStatusText.startAnimation(animFadeIn);
                    currentStatusText.setText(Constants.DeviceTypes.SERVER);
                    peerConnectTime.setText((long) msg.arg2 + " msec");
                    useFile.savePairingData(Constants.FileNames.Pairing, Constants.DeviceTypes.SERVER, msg.arg2);
                    bandwidthText.setVisibility(View.GONE);
                    BWProgressLLayout.setVisibility(View.GONE);
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
            }
        };


        serverMessageSConnect = new BluetoothConnectSmmSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverMessageSConnect.start();

        serverACKConnect = new BluetoothConnectSACKSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverACKConnect.start();

        serverBWConnect = new BluetoothConnectSBWSocket(mBluetoothAdapter, btServerConnectionStatus);
        serverBWConnect.start();
    }

    private final Handler btMessageStatus = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.MessageConstants.MESSAGE_WRITE) {
                btStatusText.setText("Message is sent");

                sentDataSize = msg.arg1;

                // Save speed of the device at that particular time when Message was received
                String showSpeed = currentspeed + " m/s";
                useFile.saveSpeedData(Constants.FileNames.Speed, showSpeed);
                Log.i(Constants.TAG, "Am I inside Message Writing Handler? " + true);

            } else if (msg.what == Constants.MessageConstants.MESSAGE_TOAST) {
                String statusMessage = bundle.getString("status");
                btStatusText.setText(statusMessage);
            } else if ((msg.what == Constants.MessageConstants.MESSAGE_READ)) {
                btStatusText.setText("Message received");
                byte[] writeBuf = (byte[]) msg.obj;
                // byte[] writeACK = new byte[]{'R'};
                String writeMessage = new String(writeBuf);

                if (writeMessage.equals(Constants.DataTypes.TEXT)) {
                    fileTypeStatus = Constants.DataTypes.TEXT;
                } else if (writeMessage.equals(Constants.DataTypes.IMAGE)) {
                    fileTypeStatus = Constants.DataTypes.IMAGE;
                }

                if (fileTypeStatus.equals(Constants.DataTypes.TEXT)) {
                    if (!(writeMessage.equals(Constants.DataTypes.TEXT))) {
                        if (messageReceived.getVisibility() != View.VISIBLE) {
                            messageReceived.setVisibility(View.VISIBLE);
                        }
                        messageReceived.startAnimation(animFadeIn);
                        // Log.i(Constants.TAG, "Message size after trimming: " + message.length);
                        messageReceived.setText(writeMessage);
                        useFile.saveReceivedMessage(Constants.FileNames.ReceivedMessage, writeMessage);
                    }
                } else if (fileTypeStatus.equals(Constants.DataTypes.IMAGE)) {
                    if (!(writeMessage.equals(Constants.DataTypes.IMAGE))) {
                        Log.i(Constants.TAG, "Converting Bytes Into Images " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
                        try {
                            img.writeFileAsBytes(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), writeBuf);
                        } catch (IOException e) {
                            Log.e(Constants.TAG, "Could Not Save Image To Specified Place. " + e.toString());
                        }
                    }

                }

                if (!(writeMessage.equals(Constants.DataTypes.IMAGE) || writeMessage.equals(Constants.DataTypes.TEXT))) {
                    // Send an ACK after data is received
                    String writeACK = Integer.toString(msg.arg1);

                    GlobalReceivedMessage = writeMessage;
                    ACKData.write(writeACK.getBytes());
                }

                if (deviceType.equals(Constants.DeviceTypes.CLIENT)) {
                    bytesReceivedText.setText(Integer.toString(msg.arg1));
                    bytesSentText.setText(Constants.Miscellaneous.NONE);
                    MsgPacketLossText.setText(Constants.Miscellaneous.NONE);
                }

                // Save speed of the device at that particular time when Message was received
                String showSpeed = currentspeed + " m/s";
                useFile.saveSpeedData(Constants.FileNames.Speed, showSpeed);
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
                if (writeBuf[0] == 'R') {
                    Log.i(Constants.TAG, "I am inside the if condition in ACK writeBuf");
                    stopWatch.halt();
                    // Update Message Timing List and Reset The Timer
                    useFile.saveDelayData(Constants.FileNames.Delay, stopWatch.getGlobalTime());
                    stopWatch.updateList();
                    stopWatch.reset();
                } else {
                    Log.i(Constants.TAG, "I am inside the else condition in ACK writeBuf");
                    stopWatch.halt();
                    // Update Message Timing List and Reset The Timer
                    useFile.saveDelayData(Constants.FileNames.Delay, stopWatch.getGlobalTime());
                    stopWatch.updateList();
                    stopWatch.reset();
                }
                GlobalMsgPacketLoss = streamData.getPacketLoss(sentDataSize, new String(writeBuf)); // For 1st Scenario
                String showMsgLossPercent = df.format(GlobalMsgPacketLoss) + "%";
                if (GlobalMsgPacketLoss == 0) {
                    MsgPacketLossText.setTextColor(Color.GRAY);
                    MsgPacketLossText.setText("0" + showMsgLossPercent);
                } else {
                    MsgPacketLossText.setTextColor(Color.RED);
                    MsgPacketLossText.setText(showMsgLossPercent);
                }

                // Show Sent & Received Bytes
                bytesReceivedText.setText(Integer.toString(streamData.getMessageReceivedBytes("" + new String(writeBuf))));
                bytesSentText.setText(Integer.toString(sentDataSize));
                useFile.savePacketLossData(Constants.FileNames.MsgPacketLoss, GlobalMsgPacketLoss);

            } else if (msg.what == Constants.MessageConstants.ACK_WRITE)

            {
                // Log.i(Constants.TAG, "I am sending an ACK -> " + GlobalReceivedMessage);
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
                FileSentBandwidth = (useFile.getBWFileSize() / bandData.getTotalBandwidthDuration());
                // Log.i(Constants.TAG, "Bandwidth Duration: " + bandData.getTotalBandwidthDuration());
                // Log.i(Constants.TAG, "Check FileSentBandwidth:" + FileSentBandwidth);
                String bandwidth = String.format("%.2f", ((FileSentBandwidth) / 1024.0)) + " KBps";
                globalBandwidth = bandwidth;
                bandwidthText.setText(bandwidth);
                getDataHandler.sendEmptyMessage((int) FileSentBandwidth); // Send anything
                progressBarHandler.sendEmptyMessage(msg.arg1);
                checkBandwidthText.setText("Sending " + Constants.Miscellaneous.BW_FileSize + " of Data");
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
                                Log.i(Constants.TAG, "FileSentBandwidth: " + (int) FileSentBandwidth);
                                sendBWProgressBarView.setProgress((int) (FileSentBandwidth / 1024));
                            }
                        };
                        Looper.loop();
                    }
                });

                if (msg.arg1 == 1 && BWStart) {
                    BWStart = false;
                    checkBandwidthText.setText(R.string.checkingBandwidth);
                    writeBandwidthToFileT.start();
                    BWProgressLLayout.setVisibility(View.VISIBLE);
                    sendBWProgressBarView.setMax(Constants.Miscellaneous.MAX_BANDWIDTH);
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

    public void checkBandwidth() {
        final Thread checkBandwidthT = new Thread(new Runnable() {
            @Override
            public void run() {

                // Check Bandwidth
                if (!useFile.checkFileExists(Constants.testFileName)) {
                    tempFile = useFile.createTemporaryFile(Constants.testFileName);
                    useFile.fillTempFile(tempFile);
                } else {
                    tempFile = useFile.returnFile(Constants.testFileName);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkBandwidthText.setVisibility(View.VISIBLE);
                        checkBandwidthText.setTextColor(Color.MAGENTA);

                        // To be done before Bandwidth Check
                        charLimitTxtView.startAnimation(animFadeOut);
                        charLimitTxtView.setVisibility(View.GONE);
                        EditMessageBox.startAnimation(animFadeIn);
                        EditMessageBox.setVisibility(View.VISIBLE);
                    }
                });
                bandData.checkBandwidth(useFile, tempFile);
//                FileSentBandwidth = (useFile.getFileSize() / bandData.getTotalBandwidthDuration());
//                Log.i(Constants.TAG, "From the thread after calculation:" + FileSentBandwidth);
//                getDataHandler.sendEmptyMessage((int) FileSentBandwidth);
//                Log.i(Constants.TAG, "Check FileSentBandwidth From Thread:" + FileSentBandwidth);
//                Log.i(Constants.TAG, (String) (useFile.getFileSize() + " Time: " + bandData.getTotalBandwidthDuration()));
            }
        });

        checkBandwidthT.start();


//        getDataHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                Log.i(Constants.TAG, "Check FileSentBandwidth:" + FileSentBandwidth);
//                String bandwidth = String.format("%.2f", (FileSentBandwidth / 1024.0)) + " KBps";
//                bandwidthText.setText(bandwidth);
//                useFile.saveBWData(Constants.FileNames.Bandwidth, bandwidth);
//
//                try {
//                    checkBandwidthT.sleep(1000);
//                    checkBandwidthT.run();
//                } catch (InterruptedException SleepE) {
//                    Log.i(Constants.TAG, "checkBandwidthT is not able to sleep");
//                }
//
//            }
//
//        };
    }

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
                    String messageToSend = EditMessageBox.getText().toString();
                    streamData.write(Constants.DataTypes.TEXT.getBytes());
                    streamData.write(messageToSend.getBytes());
                    // Log.i(Constants.TAG, "Message Sent: " + EditMessageBox.getText());
                    useFile.saveMessage(Constants.FileNames.SentMessage, EditMessageBox.getText().toString());
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), NOT_YET_CONNECTED, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        });
    }

    public void sendImage() {

        NOT_YET_CONNECTED = "I am not yet connected to any phone";

        sendImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(SocketGlobal == null)) {
                    performFileSearch();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), NOT_YET_CONNECTED, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }

        });
    }

    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                // ImageUri = uri;
                Log.i(Constants.TAG, "Uri: " + uri.toString());
                byte[] ImgBytes = img.ImageToBytes(RealPathUtil.getRealPath(getApplicationContext(), uri)); // Converting Image To Bytes
                Log.i(Constants.TAG, "ImgBytes: " + ImgBytes.length);
                if (ImgBytes != null) {
                    streamData.write(Constants.DataTypes.IMAGE.getBytes());
                    streamData.write(ImgBytes);
                } else {
                    Toast.makeText(getApplicationContext(), "Image URI is null", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void getUUIDs() {
        try {
            Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);

            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(mBluetoothAdapter, null);

            for (ParcelUuid uuid : uuids) {
                Log.d(Constants.TAG, "UUID: " + uuid.getUuid().toString());
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        mBluetoothAdapter.setName(getGoodOldName);
        mBluetoothAdapter.disable();
        if (alertDialogOpened == true) {
            alertDialog.dismiss();
        }
        if (serverMessageSConnect != null) {
            serverMessageSConnect.cancel();
        }
        if (clientMessageSConnect != null) {
            clientMessageSConnect.cancel();
        }
        if (serverACKConnect != null) {
            serverACKConnect.cancel();
        }
        if (clientACKConnect != null) {
            clientACKConnect.cancel();
        }
        if (serverBWConnect != null) {
            serverBWConnect.cancel();
        }
        if (clientBWConnect != null) {
            clientBWConnect.cancel();
        }
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }


}