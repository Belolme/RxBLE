package com.billin.www.rxble.ble.originV2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 蓝牙操作管理类
 * <p/>
 * Created by Billin on 2017/5/12.
 */
public class BluetoothLeInitialization {

    @SuppressLint("StaticFieldLeak")
    private static volatile BluetoothLeInitialization mInstance;

    private final static String TAG = "BluetoothLe";

    private final Context mContext;

    private static Handler mBluetoothWorker;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothManager mBluetoothManager;

    private Map<String, BluetoothLeConnector> mGattConnectorMap
            = new ConcurrentHashMap<>();

    private BluetoothLeSearcher mBluetoothSearcher;

    private BluetoothLeInitialization(Context context) {
        mContext = context.getApplicationContext();

        HandlerThread thread = new HandlerThread("bluetooth worker");
        thread.start();
        mBluetoothWorker = new Handler(thread.getLooper());
    }

    public static BluetoothLeInitialization getInstance(Context context) {
        if (mInstance == null) {
            synchronized (BluetoothLeInitialization.class) {
                if (mInstance == null) {
                    mInstance = new BluetoothLeInitialization(context);
                }
            }
        }

        return mInstance;
    }

    /**
     * 初始化BluetoothAdapter
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {

        // For API level 18 and above, get a reference to BluetoothAdapter
        // through BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                return false;
            }
        }

        return mBluetoothAdapter.isEnabled() || mBluetoothAdapter.enable();
    }

    public BluetoothLeSearcher getBluetoothSearcher() {
        if (mBluetoothSearcher == null) {
            synchronized (BluetoothLeInitialization.class) {
                if (mBluetoothSearcher == null) {
                    if (mBluetoothAdapter == null) {
                        // TODO: 2017/5/12 是否需要改成异常呢？
                        String err = "cannot create BluetoothLeSearcher instance because not " +
                                "initialize, please call initialize() method";
                        Log.e(TAG, err);
                        return null;
                    }

                    mBluetoothSearcher = new BluetoothLeSearcher(mContext, mBluetoothAdapter, mBluetoothWorker);
                }
            }
        }

        return mBluetoothSearcher;
    }

    public BluetoothLeConnector getBluetoothLeConnector(String mac) {
        BluetoothLeConnector result;
        if ((result = mGattConnectorMap.get(mac)) != null) {
            return result;
        }

        result = new BluetoothLeConnector(mContext, mBluetoothAdapter, mac, mBluetoothWorker);
        mGattConnectorMap.put(mac, result);
        return result;
    }

    public void cleanConnector(String mac) {
        BluetoothLeConnector result;
        if ((result = mGattConnectorMap.get(mac)) != null) {
            mGattConnectorMap.remove(mac);
            result.disconnect();
            result.setOnDataAvailableListener(null);
        }
    }

    /**
     * 在不在需要连接蓝牙设备的时候，
     * 或者生命周期暂停的时候调用这一个方法
     */
    public void cleanAllConnector() {
        for (String mac : mGattConnectorMap.keySet()) {
            cleanConnector(mac);
        }
    }
}
