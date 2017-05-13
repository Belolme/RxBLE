package com.billin.www.rxble.ble;


import com.billin.www.rxble.ble.bean.BLEDevice;
import com.billin.www.rxble.ble.callback.BaseResultCallback;
import com.billin.www.rxble.ble.exception.BluetoothSearchConflictException;

import java.util.UUID;

import io.reactivex.Observable;


/**
 * 蓝牙控制类. 使用这一个类连接蓝牙设备的时候，最好在连接之前扫描一下附件的设备，
 * 如果能够扫描得到才进行连接，降低连接蓝牙的出错率。
 * <p/>
 * Created by Billin on 2017/4/14.
 */
public interface BluetoothClient {

    /**
     * 打开蓝牙扫描操作. 如果此时正在扫描将会抛出正在扫描 {@link BluetoothSearchConflictException} 错误。
     * 如果想强制中断当前扫描操作，set cancel value to true.
     *
     * @param millis 扫描时间
     * @param cancel 如果正在进行扫描操作，设置是否中断当前扫描。true 中断当前扫描操作，
     *               false 如果当前正在进行扫描操作则会抛出 {@link BluetoothSearchConflictException} 错误
     * @return 扫描结果的列表（无重复设备）
     */
    Observable<BLEDevice> search(int millis, boolean cancel);

    void stopSearch();

    /**
     * 连接一台蓝牙设备. 连接的蓝牙设备有最大限制，
     * 如果超出这一个数量，即使连接上了蓝牙设备也扫描不到该设备的服务通道
     *
     * @param mac 需要连接蓝牙设备的地址
     * @return 成功，返回连接设备的地址
     */
    Observable<String> connect(String mac);

    /**
     * 断开蓝牙连接, 释放蓝牙连接占用的蓝牙服务
     *
     * @param mac 需要断开连接的 mac 地址
     */
    void disconnect(String mac);

    /**
     * 向一个蓝牙设备写入值
     *
     * @param mac            设备 mac 地址
     * @param service        设备服务地址
     * @param characteristic 设备 characteristic 地址
     * @param values         需要写入的值
     * @return 写入成功返回
     */
    Observable<String> write(String mac, UUID service, UUID characteristic, byte[] values);

    /**
     * 向蓝牙设备注册一个通道值改变的监听器,
     * 每一个设备的每一个通道只允许同时存在一个监听器。
     *
     * @param mac            需要监听的 mac 地址
     * @param service        需要监听的设备的服务地址
     * @param characteristic 需要监听设备的 characteristic
     * @param callback       需要注册的监听器
     * @return 成功或失败返回
     */
    Observable<String> registerNotify(String mac, UUID service, UUID characteristic,
                                      BaseResultCallback<byte[]> callback);

    /**
     * 解除在对应设备对应通道注册了的监听器
     *
     * @param mac            需要监听的 mac 地址
     * @param service        需要监听的设备的服务地址
     * @param characteristic 需要监听设备的 characteristic
     */
    Observable<String> unRegisterNotify(String mac, UUID service, UUID characteristic);

    /**
     * 清空对应 MAC 地址的蓝牙设备缓存
     *
     * @param mac 蓝牙设备硬件地址
     */
    void clean(String mac);

    /**
     * 清空所有缓存的蓝牙设备
     */
    void cleanAll();

    /**
     * 启动蓝牙
     */
    void openBluetooth();

    /**
     * 关闭蓝牙
     */
    void closeBluetooth();
}
