package me.iologic.apps.dtn;

import java.util.UUID;

public class UUIDManager {

    public UUID mmSocket_UUID;
    public UUID ACK_UUID;
    public UUID BW_UUID;

    private String deviceName;

    public UUIDManager(String receivedDeviceName) {
        deviceName = receivedDeviceName;

        setUUIDs();
    }

    private void setUUIDs() {
        switch (deviceName) {
            case Constants.DeviceNames.originDevice:
                mmSocket_UUID = Constants.UUIDs.originMMSocket_UUID;
                ACK_UUID = Constants.UUIDs.origin_ACK_UUID;
                BW_UUID = Constants.UUIDs.origin_BW_UUID;
            case Constants.DeviceNames.destinationDevice:
                mmSocket_UUID = Constants.UUIDs.mmSocket_UUID;
                ACK_UUID = Constants.UUIDs.ACK_UUID;
                BW_UUID = Constants.UUIDs.BW_UUID;
            default:
                break;
        }
    }
}
