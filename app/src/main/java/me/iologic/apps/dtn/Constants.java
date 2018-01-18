package me.iologic.apps.dtn;

/**
 * Created by vinee on 16-01-2018.
 */

public class Constants {
    public static final String TAG = "DTNLogs";
    public static final String testFileName = "testFile";

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    public interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        public static final int PACKET_SIZE = 2; // 2 Bytes Per Packet.
        public static final int NO_OF_PACKETS = 25;
    }
}
