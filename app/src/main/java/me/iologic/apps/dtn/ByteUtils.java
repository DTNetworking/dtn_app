package me.iologic.apps.dtn;

import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by vinee on 19-01-2018.
 */

public class ByteUtils {
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        Log.i(Constants.TAG, "Received Bytes: " + new String(bytes));
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }
}
