package com.billin.www.rxble.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.os.HandlerThread;
import android.util.Log;

import com.billin.www.rxble.ble.bean.BLEDevice;
import com.billin.www.rxble.ble.callback.BaseResultCallback;
import com.billin.www.rxble.ble.exception.BluetoothNotOpenException;
import com.billin.www.rxble.ble.exception.BluetoothSearchConflictException;
import com.billin.www.rxble.ble.exception.BluetoothWriteExceptionWithMac;
import com.billin.www.rxble.ble.originV2.BluetoothLeConnector;
import com.billin.www.rxble.ble.originV2.BluetoothLeInitialization;
import com.billin.www.rxble.ble.originV2.BluetoothLeSearcher;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class BluetoothClientBLEV2Adapter implements BluetoothClient {

    private static final String TAG = "BluetoothClient";

    private BluetoothLeInitialization mClient;

    public BluetoothClientBLEV2Adapter(BluetoothLeInitialization client) {
        mClient = client;

        HandlerThread workThread = new HandlerThread("bluetooth worker");
        workThread.start();
    }

    @Override
    public Observable<BLEDevice> search(final int millis, final boolean cancel) {
        return Observable.create(new ObservableOnSubscribe<BLEDevice>() {
            @Override
            public void subscribe(final ObservableEmitter<BLEDevice> e) throws Exception {

                BluetoothLeSearcher searcher = mClient.getBluetoothSearcher();

                if (searcher.isScanning() && !cancel) {
                    e.onError(new BluetoothSearchConflictException("is searching now"));
                    return;
                }

                if (searcher.isScanning()) {
                    stopSearch();
                }

                mClient.getBluetoothSearcher()
                        .scanLeDevice(millis, new BluetoothLeSearcher.OnScanCallback() {
                            private Set<BLEDevice> devices = new HashSet<>();

                            @Override
                            public void onLeScan(BluetoothDevice device,
                                                 int rssi,
                                                 byte[] scanRecord) {

                                BLEDevice bleDevice = new BLEDevice();
                                bleDevice.setDeviceName(device.getName());
                                bleDevice.setMac(device.getAddress());
                                bleDevice.setRssi(rssi);

                                if (devices.contains(bleDevice)) {
                                    return;
                                }

                                devices.add(bleDevice);
                            }

                            @Override
                            public void onComplete() {
                                for (BLEDevice device : devices) {
                                    e.onNext(device);
                                }
                                e.onComplete();
                            }

                            @Override
                            public void onError(String msg) {
                                e.onError(new BluetoothNotOpenException(msg));
                            }
                        });
            }
        });
    }

    @Override
    public void stopSearch() {
        mClient.getBluetoothSearcher().stopScanLeDevice();
    }

    @Override
    public Observable<String> connect(final String mac) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                BluetoothLeConnector connector = mClient.getBluetoothLeConnector(mac);

                connector.setOnDataAvailableListener(new BluetoothLeConnector.OnDataAvailableListener() {
                    @Override
                    public void onCharacteristicRead(byte[] values, int status) {

                    }

                    @Override
                    public void onCharacteristicChange(UUID characteristic, byte[] values) {

                    }

                    @Override
                    public void onCharacteristicWrite(UUID characteristic, int status) {

                    }

                    @Override
                    public void onDescriptorWrite(UUID descriptor, int status) {

                    }

                    @Override
                    public void onError(String msg) {

                    }
                });

                connector.connect(new BluetoothLeConnector.OnConnectListener() {
                    @Override
                    public void onConnect() {

                    }

                    @Override
                    public void onDisconnect() {

                    }

                    @Override
                    public void onServiceDiscover() {
                        e.onNext(mac);
                        e.onComplete();
                    }

                    @Override
                    public void onError(String msg) {
                        e.onError(new Exception(msg));
                    }
                });
            }
        });
    }

    @Override
    public void disconnect(String mac) {
        mClient.getBluetoothLeConnector(mac).disconnect();
    }

    @Override
    public Observable<String> write(final String mac, final UUID service, final UUID characteristic,
                                    final byte[] values) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                BluetoothLeConnector connector = mClient.getBluetoothLeConnector(mac);

                final BluetoothLeConnector.OnDataAvailableListener onConnectListener
                        = connector.getOnDataAvailableListener();

                connector.setOnDataAvailableListener(new BluetoothLeConnector.OnDataAvailableListener() {
                    @Override
                    public void onCharacteristicRead(byte[] values, int status) {
                        onConnectListener.onCharacteristicRead(values, status);
                    }

                    @Override
                    public void onCharacteristicChange(UUID characteristic, byte[] values) {
                        onConnectListener.onCharacteristicChange(characteristic, values);
                    }

                    @Override
                    public void onCharacteristicWrite(UUID characteristic, int status) {
                        e.onNext(mac);
                        e.onComplete();
                    }

                    @Override
                    public void onDescriptorWrite(UUID descriptor, int status) {
                        onConnectListener.onDescriptorWrite(descriptor, status);
                    }

                    @Override
                    public void onError(String msg) {
                        e.onError(new Exception(msg));
                    }
                });
                connector.writeCharacteristic(service, characteristic, values);
            }
        });
    }

    @Override
    public Observable<String> registerNotify(final String mac, final UUID service, final UUID characteristic,
                                             final BaseResultCallback<byte[]> callback) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {

                BluetoothLeConnector connector = mClient.getBluetoothLeConnector(mac);

                final BluetoothLeConnector.OnDataAvailableListener onDataAvailableListener = connector
                        .getOnDataAvailableListener();

                connector.setOnDataAvailableListener(new BluetoothLeConnector.OnDataAvailableListener() {
                    @Override
                    public void onCharacteristicRead(byte[] values, int status) {
                        onDataAvailableListener.onCharacteristicRead(values, status);
                    }

                    @Override
                    public void onCharacteristicChange(UUID characteristic, byte[] values) {
                        callback.onSuccess(values);
                    }

                    @Override
                    public void onCharacteristicWrite(UUID cha, int status) {
                        onDataAvailableListener.onCharacteristicWrite(cha, status);
                    }

                    @Override
                    public void onDescriptorWrite(UUID descriptor, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.d(TAG, "registerNotify pass");
                            e.onNext(mac);
                            e.onComplete();
                        } else {
                            String err = "write exception mac " + mac + " with " + status;
                            Log.e(TAG, err);
                            e.onError(new BluetoothWriteExceptionWithMac(err, mac));
                        }
                    }

                    @Override
                    public void onError(String msg) {
                        e.onError(new Exception(msg));
                    }
                });
                connector.setCharacteristicNotification(service, characteristic, true);
            }
        });
    }

    @Override
    public Observable<String> unRegisterNotify(String mac, UUID service, UUID characteristic) {
        return null;
    }

    @Override
    public void clean(String mac) {
        mClient.cleanConnector(mac);
    }

    @Override
    public void cleanAll() {
        mClient.cleanAllConnector();
    }

    @Override
    public void openBluetooth() {
        mClient.initialize();
    }

    @Override
    public void closeBluetooth() {

    }
}
