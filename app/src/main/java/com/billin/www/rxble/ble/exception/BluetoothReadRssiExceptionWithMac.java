package com.billin.www.rxble.ble.exception;

/**
 * 蓝牙读取信号错误异常
 * <p/>
 * Created by Billin on 2017/4/14.
 */
public class BluetoothReadRssiExceptionWithMac extends BluetoothExceptionWithMac {

    public BluetoothReadRssiExceptionWithMac(String msg, String mac) {
        super(msg, mac);
    }
}
