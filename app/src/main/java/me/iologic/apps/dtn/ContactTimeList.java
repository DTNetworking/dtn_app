package me.iologic.apps.dtn;

public class ContactTimeList {
    private String deviceName;
    private String deviceConnectedTime;
    private String interContactTime;

    public ContactTimeList(String deviceName, String deviceConnectedTime, String interContactTime){
        this.deviceName = deviceName;
        this.deviceConnectedTime = deviceConnectedTime;
        this.interContactTime = interContactTime;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceConnectedTime() {
        return deviceConnectedTime;
    }

    public String getInterContactTime() {
        return interContactTime;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceConnectedTime(String deviceConnectedTime) {
        this.deviceConnectedTime = deviceConnectedTime;
    }

    public void setInterContactTime(String interContactTime) {
        this.interContactTime = interContactTime;
    }
}
