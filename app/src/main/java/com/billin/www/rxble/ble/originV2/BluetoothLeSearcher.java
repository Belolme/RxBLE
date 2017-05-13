package com.billin.www.rxble.ble.originV2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 蓝牙扫描服务封装类
 * <p/>
 * Created by Billin on 2017/5/12.
 */
public class BluetoothLeSearcher {
    public interface OnScanCallback extends BluetoothAdapter.LeScanCallback {
        void onComplete();

        void onError(String msg);
    }

    private static final String TAG = "BluetoothLe";

    private BluetoothAdapter mBluetoothAdapter;

    private final Handler mHandler;

    private Handler mAlertHandler;

    private OnScanCallback mScanCallback;

    private Context mContext;

    private AtomicBoolean mScanning = new AtomicBoolean(false);

    BluetoothLeSearcher(Context context, BluetoothAdapter adapter, Handler worker) {
        mContext = context;
        mBluetoothAdapter = adapter;
        mHandler = worker;

        HandlerThread thread = new HandlerThread("bluetooth searcher handler");
        thread.start();
        mAlertHandler = new Handler(thread.getLooper());
    }

    private OnScanCallback wrapCallback(final OnScanCallback callback) {

        return new OnScanCallback() {
            @Override
            public void onComplete() {
                runOn(new Runnable() {
                    @Override
                    public void run() {
                        callback.onComplete();
                    }
                });
            }

            @Override
            public void onError(final String msg) {
                runOn(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(msg);
                    }
                });
            }

            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                runOn(new Runnable() {
                    @Override
                    public void run() {
                        callback.onLeScan(device, rssi, scanRecord);
                    }
                });
            }
        };
    }

    /**
     * 指定开始扫描蓝牙服务. 如果一个扫描服务正在运行,
     * 马上停止当前的扫描服务, 只进行新的扫描服务.
     */
    public void scanLeDevice(final int scanMillis,
                             final OnScanCallback callback) {

        runOn(new Runnable() {
            @Override
            public void run() {
                int permissionCheck = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED
                        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String err = "Cannot have location permission";
                    Log.e(TAG, err);
                    callback.onError(err);
                    return;
                }

                if (mScanning.get()) {
                    stopScan();
                }

                mScanCallback = wrapCallback(callback);

                // Stops scanning after a pre-defined scan period.
                // 预先定义停止蓝牙扫描的时间（因为蓝牙扫描需要消耗较多的电量）
                mAlertHandler.removeCallbacksAndMessages(null);
                mAlertHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScanLeDevice();
                    }
                }, scanMillis);

                mScanning.set(true);

                // 定义一个回调接口供扫描结束处理
                // 指定扫描特定的支持service的蓝牙设备
                // call startLeScan(UUID[],    BluetoothAdapter.LeScanCallback)
                // 可以使用rssi计算蓝牙设备的距离
                // 计算公式：
                // d = 10^((abs(RSSI) - A) / (10 * n))
                // 其中：
                // d - 计算所得距离
                // RSSI - 接收信号强度（负值）
                // A - 射端和接收端相隔1米时的信号强度
                // n - 环境衰减因子
                if (!mBluetoothAdapter.startLeScan(mScanCallback)) {
                    callback.onError("Bluetooth is not opened!");
                }
            }
        });
    }

    public void stopScan() {
        if (mScanning.get()) {

            mScanning.set(false);
            mScanCallback.onComplete();

            mAlertHandler.removeCallbacksAndMessages(null);
            mBluetoothAdapter.stopLeScan(mScanCallback);

            mScanCallback = null;
        }
    }

    public void stopScanLeDevice() {
        runOn(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        });
    }

    public boolean isScanning() {
        return mScanning.get();
    }

    private void runOn(Runnable runnable) {
        mHandler.post(runnable);
    }
}
