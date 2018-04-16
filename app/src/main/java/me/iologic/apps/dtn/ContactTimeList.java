package me.iologic.apps.dtn;

import android.os.Parcel;
import android.os.Parcelable;

public class ContactTimeList implements Parcelable{
    private String deviceName;
    private String deviceConnectedTime;
    private String interContactTime;

    public ContactTimeList(String deviceName, String deviceConnectedTime, String interContactTime){
        this.deviceName = deviceName;
        this.deviceConnectedTime = deviceConnectedTime;
        this.interContactTime = interContactTime;
    }

    /**
     * Constructs a Question from a Parcel
     * @param parcel Source Parcel
     */
    public ContactTimeList (Parcel parcel) {
        this.deviceName = parcel.readString();
        this.deviceConnectedTime = parcel.readString();
        this.interContactTime = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
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

    // Required method to write to Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeString(deviceConnectedTime);
        dest.writeString(interContactTime);
    }

    // Method to recreate a Question from a Parcel
    public static Creator<ContactTimeList> CREATOR = new Creator<ContactTimeList>() {

        @Override
        public ContactTimeList createFromParcel(Parcel source) {
            return new ContactTimeList(source);
        }

        @Override
        public ContactTimeList[] newArray(int size) {
            return new ContactTimeList[size];
        }

    };
}
