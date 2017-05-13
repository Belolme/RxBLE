package com.billin.www.rxble;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.billin.www.rxble.ble.BluetoothClient;
import com.billin.www.rxble.ble.BluetoothClientBLEV2Adapter;
import com.billin.www.rxble.ble.bean.BLEDevice;
import com.billin.www.rxble.ble.originV2.BluetoothLeInitialization;

import java.util.UUID;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;

public class MainActivity extends AppCompatActivity {

    BluetoothClient mClient;

    private static final String TAG = "MainActivity";

    private TextView mTextView;

    private static final String[] MAC = {
            "98:5D:AD:23:21:DA",
            "98:5D:AD:23:21:DD",
            "98:5D:AD:23:21:AB",
            "C8:FD:19:43:68:E2",
            "98:5D:AD:23:23:80",
    };

    private static final UUID UUID_SERVICE_CHANNEL
            = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    private static final UUID UUID_CHARACTERISTIC_CHANNEL
            = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClient = new BluetoothClientBLEV2Adapter(BluetoothLeInitialization.getInstance(this));
        mClient.openBluetooth();

        Button writeButton = new Button(this);
        writeButton.setText("write data");
        writeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectAndWrite();
            }
        });

        Button scanButton = new Button(this);
        scanButton.setText("scan");
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

        mTextView = new TextView(this);
        mTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        layout.addView(writeButton);
        layout.addView(scanButton);
        layout.addView(mTextView);
        setContentView(layout);
    }

    private void connectAndWrite() {
        mClient.connect(MAC[1])
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        Log.d(TAG, "connect test: on write");
                        return mClient.write(MAC[1], UUID_SERVICE_CHANNEL,
                                UUID_CHARACTERISTIC_CHANNEL,
                                "01234567876543210#".getBytes());
                    }
                })
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.d(TAG, "connect test onSubscribe: ");
                    }

                    @Override
                    public void onNext(String value) {
                        Log.d(TAG, "connect test onNext: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "connect test onError: ", e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "connect test onComplete: ");
                    }
                });
    }

    void search() {
        // 第一参数指定扫描时间，第二个参数指定是否中断当前正在进行的扫描操作
        mClient.search(3000, false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BLEDevice>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mTextView.setText("start\n");
                    }

                    @Override
                    public void onNext(BLEDevice value) {
                        Log.d(TAG, "device " + value);
                        mTextView.setText(mTextView.getText() + "\n\n" + value);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                        mTextView.setText(mTextView.getText() + "\n\n" + "complete");
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: search");
                        mTextView.setText(mTextView.getText() + "\n\n" + "complete");
                    }
                });
    }
}
