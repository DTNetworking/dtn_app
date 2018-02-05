package me.iologic.apps.dtn;

/**
 * Created by vinee on 16-01-2018.
 */

public class Constants {
    public static final String TAG = "DTNLogs";
    public static final String testFileName = "testFile";


    public interface FileNames {
        public static final String Bandwidth = "Bandwidth";
        public static final String Delay = "MsgTimings";
    }

    public interface Packet {
        public static final int PACKET_SIZE = 2; // 2 Bytes Per Packet.
        public static final int NO_OF_PACKETS = 25;
        public static final int BW_PACKET_SIZE = 1024 * 512; // 512 KB
        public static final int BW_FILE_SIZE = 1024 * 1024;
        public static final int BW_COUNTER = BW_FILE_SIZE / BW_PACKET_SIZE;
    }

    public interface Permissions {
        public static final int PERMISSION_REQUEST_CODE = 200;
        public static final int REQUEST_ENABLE_BT = 1;
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

        public static final String DISCOVERY_SUCCESS_MESSAGE = "Discoverability set to ON";
        public static final String DISCOVERY_FAIL_MESSAGE = "Discoverability failed";

        public static final String ACK_CONNECT_SERVER_SUCCESS = "Acknowledgement connection is successful as a server";
        public static final String ACK_CONNECT_CLIENT_SUCCESS = "Acknowledgement connection is successful as a client";

        public static final String BW_CONNECT_SERVER_SUCCESS = "Bandwidth connection is successful as a server";
        public static final String BW_CONNECT_CLIENT_SUCCESS = "Bandwidth connection is successful as a client";
    }
}
