package me.iologic.apps.dtn;

import android.util.Log;

import java.util.UUID;

public class UUIDManager {

    public UUID mmSocket_UUID;
    public UUID ACK_UUID;
    public UUID BW_UUID;

    public UUID second_mmSocket_UUID;
    public UUID second_ACK_UUID;
    public UUID second_BW_UUID;

    private String deviceName;

    public UUIDManager(String receivedDeviceName) {
        deviceName = receivedDeviceName;

        setUUIDs();
    }

    private void setUUIDs() {
        switch (deviceName) {
            case Constants.DeviceNames.secondRouterDevice:
                mmSocket_UUID = Constants.UUIDs.mmSocket_UUID;
                ACK_UUID = Constants.UUIDs.ACK_UUID;
                BW_UUID = Constants.UUIDs.BW_UUID;
                second_mmSocket_UUID = Constants.UUIDs.secondMMSocket_UUID;
                second_ACK_UUID = Constants.UUIDs.second_ACK_UUID;
                second_BW_UUID = Constants.UUIDs.second_BW_UUID;
                break;
            case Constants.DeviceNames.thirdRouterDevice:
                mmSocket_UUID = Constants.UUIDs.secondMMSocket_UUID;
                ACK_UUID = Constants.UUIDs.second_ACK_UUID;
                BW_UUID = Constants.UUIDs.second_BW_UUID;
                second_mmSocket_UUID = Constants.UUIDs.third_MMSocket_UUID;
                second_ACK_UUID = Constants.UUIDs.third_ACK_Socket_UUID;
                second_BW_UUID = Constants.UUIDs.third_BW_Socket_UUID;
                break;
            default:
                break;
        }

        Log.i(Constants.TAG, "mmSocket: " + mmSocket_UUID);
    }
}