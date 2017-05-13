package com.billin.www.rxble.ble.exception;

public class BluetoothExceptionWithMac extends BluetoothException {

    String mac;

    public String getMac() {
        return mac;
    }

    public BluetoothExceptionWithMac(String msg, String mac) {
        super(msg);
        this.mac = mac;
    }

}
