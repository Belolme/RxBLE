
# RxBLE
这是一个使用 RxJava 封装的低功耗蓝牙类库。封装了低功耗蓝牙的连接，写入数据，读取数据和监听硬件特定通道数据改变的功能。关于低功耗蓝牙的入门介绍可以参阅 [我的简书博客](http://www.jianshu.com/p/3a372af38103)

# 使用方法
clone 下来，复制 ble 包到本地项目即可使用（确保当前开发的项目有依赖 RxJava2）。可根据自己的需求进行二次开发。

# 初始化蓝牙
```java
BluetoothClient mClient;

mClient = new BluetoothClientBLEV2Adapter(
	BluetoothLeInitialization.getInstance(this));
mClient.openBluetooth();
```

# 扫描设备

```java
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
```

# 连接并写入数据示例

```java
    private void connectAndWrite() {
        mClient.connect(MAC[1])
                .flatMap(new Function<String, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(String s) throws Exception {
                        Log.d(TAG, "connect test: on write");
                        return mClient.write(MAC[1], UUID_SERVICE_CHANNEL,
                                UUID_CHARACTERISTIC_CHANNEL, "01234567876543210#".getBytes());
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
```

# 连接并设置蓝牙特定通道数据的监听

```java
mClient.connect(MAC[1])
        .flatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return mClient.registerNotify(MAC[1], UUID_SERVICE_CHANNEL,
                        UUID_CHARACTERISTIC_CHANNEL, new BaseResultCallback<byte[]>() {
                            @Override
                            public void onSuccess(byte[] data) {
                                Log.d(TAG, "I have receive a new message: "
                                        + Arrays.toString(data));
                            }

                            @Override
                            public void onFail(String msg) {
                                Log.d(TAG, "oop! setting register is failed!");
                            }
                        });
            }
        })
```
