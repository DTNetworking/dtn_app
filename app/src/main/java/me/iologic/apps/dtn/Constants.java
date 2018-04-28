package me.iologic.apps.dtn;

import java.util.UUID;

/**
 * Created by vinee on 16-01-2018.
 */

public class Constants {

    String NAME = "DTNApp";
    String TAG = "DTNLogs";
    String testFileName = "testFile";


    public interface Earth {
        double RADIUS = 6371000;
    }

    public interface FileNames {
        String Bandwidth = "Bandwidth";
        String Delay = "MsgTimings";
        String Pairing = "PairingTime";
        String MsgPacketLoss = "MsgPacketLoss";
        String BWPacketLoss = "BWPacketLoss";
        String Speed = "LightningMcQueen";
        String SentMessage = "SentMessage";
        String ReceivedMessage = "ReceivedMessage";
        String receivedImageFileName = "Img";
        String InterContactTime = "InterContactTime";
    }

    public interface Packet {
        int PACKET_SIZE = 2; // 2 Bytes Per Packet.
        int NO_OF_PACKETS = 25;
        int MSG_PACKET_SIZE = 16; // 16 bytes. For testing packet loss in 1st Scenario.
        int BW_PACKET_SIZE = 1024 * 10; // 10 KB
        int BW_FILE_SIZE = 1024 * 10;
        int BW_COUNTER = BW_FILE_SIZE / BW_PACKET_SIZE;
    }

    public interface DataTypes {
        String TEXT = "Text";
        String IMAGE = "Image";
        String AUDIO = "Audio";
    }

    public interface Permissions {
        int READ_REQUEST_CODE = 42;
        int REQUEST_RECORD_AUDIO_PERMISSION = 300;
        int REQUEST_LOCATION_PERMISSION = 200;
        int REQUEST_READ_WRITE_STORAGE = 500;
    }

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    public interface MessageConstants {
        int MESSAGE_READ = 0;
        int MESSAGE_WRITE = 1;
        int MESSAGE_TOAST = 2;

        int ACK_READ = 5;
        int ACK_WRITE = 6;
        int ACK_FAIL_TO_SEND = 7;

        int BW_READ = 10;
        int BW_WRITE = 11;
        int BW_FAIL_TO_SEND = 12;
        int BW_START_WRITE = 14;
        int BW_PACKET_LOSS_CHECK = 15;
        String BW_PACKET_RECEIVED = "B";

        String DISCOVERY_SUCCESS_MESSAGE = "Discoverability set to ON";
        String DISCOVERY_FAIL_MESSAGE = "Discoverability failed";

        String ACK_CONNECT_SERVER_SUCCESS = "Acknowledgement connection is successful as a server";
        String ACK_CONNECT_CLIENT_SUCCESS = "Acknowledgement connection is successful as a client";

        String BW_CONNECT_SERVER_SUCCESS = "Bandwidth connection is successful as a server";
        String BW_CONNECT_CLIENT_SUCCESS = "Bandwidth connection is successful as a client";

        String CLIENT_CONNECTION_FAIL = "Client Connection Failed!";

        String SERVER_CONNECTION_SUCCESSFUL = "Server is successfully connected!";
        String SERVER_CONNECTION_FAIL = "Server failed to connect";

        String NOT_YET_CONNECTED = "I am not yet connected to any phone";
        String MESSAGE_IS_EMPTY = "Message is empty. Enter a message!";
    }

    public interface EmulationMessages {
        String CLIENTCONNECT_NOT_CONNECTED = "Socket is not open yet";
        String CLIENTCONNECT_GETTING_DISCONNECTED = "The main socket which is used to send/receive messages is being disconnected now. I will try to re-connect.";
    }

    public interface UUIDs {
        UUID mmSocket_UUID = UUID.fromString("6e7bd336-5676-407e-a41c-0691e1964345"); // UUID is uniquely generated
        UUID ACK_UUID = UUID.fromString("b03901e4-710c-4509-9718-a3d15882d050");
        UUID BW_UUID = UUID.fromString("aa401ee7-3bb2-410c-9dda-2128726513a1");

        UUID destination_MMSocket_UUID = UUID.fromString("fa249bcd-e53c-4965-a9f9-d7ea5d6f0040");
        UUID destination_ACK_UUID = UUID.fromString("d9c13848-d7be-48a1-ac11-5f0c082791c7");
        UUID destination_BW_UUID = UUID.fromString("5c6ae5f9-cb04-4a71-9552-ffe426b02b99");
    }

    public interface DeviceNames {
        String originDevice = "DTN-PLEGAR1762212642";
        String secondRouterDevice = "DTN-1641b121";
        String thirdRouterDevice = "DTN-51a33087";
        String destinationDevice = "DTN-5da9d6090804";

    }

    public interface DeviceTypes {
        String SERVER = "SERVER";
        String CLIENT = "CLIENT";
    }

    public interface Miscellaneous {
        String NONE = "*";
        int MAX_BANDWIDTH = 300; //To set max 300 KBps
        String BW_FileSize = "64 KB";
        int BW_TIME_INTERVAL = 200;
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    }
}
