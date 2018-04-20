package me.iologic.apps.dtn;

import java.util.UUID;

/**
 * Created by vinee on 16-01-2018.
 */

public class Constants {

    public static final String NAME = "DTNApp";
    public static final String TAG = "DTNLogs";
    public static final String testFileName = "testFile";


    public interface Earth {
        public static final double RADIUS = 6371000;
    }

    public interface FileNames {
        public static final String Bandwidth = "Bandwidth";
        public static final String Delay = "MsgTimings";
        public static final String Pairing = "PairingTime";
        public static final String MsgPacketLoss = "MsgPacketLoss";
        public static final String BWPacketLoss = "BWPacketLoss";
        public static final String Speed = "LightningMcQueen";
        public static final String SentMessage = "SentMessage";
        public static final String ReceivedMessage = "ReceivedMessage";
        public static final String receivedImageFileName = "Img";
        public static final String InterContactTime = "InterContactTime";
    }

    public interface Packet {
        public static final int PACKET_SIZE = 2; // 2 Bytes Per Packet.
        public static final int NO_OF_PACKETS = 25;
        public static final int MSG_PACKET_SIZE = 16; // 16 bytes. For testing packet loss in 1st Scenario.
        public static final int BW_PACKET_SIZE = 1024 * 64; // 64 KB
        public static final int BW_FILE_SIZE = 1024 * 1024;
        public static final int BW_COUNTER = BW_FILE_SIZE / BW_PACKET_SIZE;
    }

    public interface DataTypes {
        public static final String TEXT = "Text";
        public static final String IMAGE = "Image";
    }

    public interface Permissions {
        public static final int PERMISSION_REQUEST_CODE = 200;
        public static final int REQUEST_ENABLE_BT = 1;
        public static final int READ_REQUEST_CODE = 42;
    }

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    public interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        public static final int ACK_READ = 5;
        public static final int ACK_WRITE = 6;
        public static final int ACK_FAIL_TO_SEND = 7;

        public static final int BW_READ = 10;
        public static final int BW_WRITE = 11;
        public static final int BW_FAIL_TO_SEND = 12;
        public static final int BW_START_WRITE = 14;
        public static final int BW_PACKET_LOSS_CHECK = 15;
        public static final String BW_PACKET_RECEIVED = "B";

        public static final String DISCOVERY_SUCCESS_MESSAGE = "Discoverability set to ON";
        public static final String DISCOVERY_FAIL_MESSAGE = "Discoverability failed";

        public static final String ACK_CONNECT_SERVER_SUCCESS = "Acknowledgement connection is successful as a server";
        public static final String ACK_CONNECT_CLIENT_SUCCESS = "Acknowledgement connection is successful as a client";

        public static final String BW_CONNECT_SERVER_SUCCESS = "Bandwidth connection is successful as a server";
        public static final String BW_CONNECT_CLIENT_SUCCESS = "Bandwidth connection is successful as a client";

        public static final String CLIENT_CONNECTION_FAIL = "Client Connection Failed!";

        public static final String SERVER_CONNECTION_SUCCESSFUL = "Server is successfully connected!";
        public static final String SERVER_CONNECTION_FAIL = "Server failed to connect";
    }

    public interface EmulationMessages {
        public static final String CLIENTCONNECT_NOT_CONNECTED = "Socket is not open yet";
        public static final String CLIENTCONNECT_GETTING_DISCONNECTED = "The main socket which is used to send/receive messages is being disconnected now. I will try to re-connect.";
    }

    public interface UUIDs {
        public static final UUID mmSocket_UUID = UUID.fromString("6e7bd336-5676-407e-a41c-0691e1964345"); // UUID is uniquely generated
        public static final UUID ACK_UUID = UUID.fromString("b03901e4-710c-4509-9718-a3d15882d050");
        public static final UUID BW_UUID = UUID.fromString("aa401ee7-3bb2-410c-9dda-2128726513a1");

        public static final UUID destination_MMSocket_UUID = UUID.fromString("085a7788-8a7e-4bb6-95e9-7c967912bf3f");
        public static final UUID destination_ACK_UUID = UUID.fromString("928bef3c-e408-44f6-b339-06358055da16");
        public static final UUID destination_BW_UUID = UUID.fromString("ddbb9433-d6c4-4fc5-b6a9-d96bdbc9d928");
    }

    public interface DeviceNames {
        public static final String originDevice = "DTN-PLEGAR1762212642";
        public static final String secondRouterDevice = "DTN-1641b121";
        public static final String thirdRouterDevice = "DTN-51a33087";
        public static final String destinationDevice = "DTN-5da9d6090804";

    }

    public interface DeviceTypes {
        public static final String SERVER = "SERVER";
        public static final String CLIENT = "CLIENT";
    }

    public interface Miscellaneous {
        public static final String NONE = "*";
    }
}
