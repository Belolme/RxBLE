package com.billin.www.rxble.ble.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 存储蓝牙设备的信息
 * <p/>
 * Created by Billin on 2017/3/3.
 */
public class BLEDevice implements Parcelable {

    private String deviceName;

    private String mac;

    private int rssi;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public static Creator<BLEDevice> getCREATOR() {
        return CREATOR;
    }

    public BLEDevice() {

    }

    private BLEDevice(Parcel in) {
        int[] intArr = new int[1];
        in.readIntArray(intArr);
        setRssi(intArr[0]);

        String[] strings = new String[3];
        in.readStringArray(strings);
        setDeviceName(strings[0]);
        setMac(strings[1]);
    }

    public static final Creator<BLEDevice> CREATOR = new Creator<BLEDevice>() {
        @Override
        public BLEDevice createFromParcel(Parcel in) {
            return new BLEDevice(in);
        }

        @Override
        public BLEDevice[] newArray(int size) {
            return new BLEDevice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int[] intArr = new int[1];
        dest.writeIntArray(intArr);
        setRssi(intArr[0]);

        String[] strings = new String[3];
        dest.writeStringArray(strings);
        setDeviceName(strings[0]);
        setMac(strings[1]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BLEDevice device = (BLEDevice) o;

        return mac.equals(device.mac);

    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }

    @Override
    public String toString() {
        return "BLEDevice{" +
                "deviceName='" + deviceName + '\'' +
                ", mac='" + mac + '\'' +
                ", rssi=" + rssi +
                '}';
    }
}
