package com.billin.www.rxble.ble.callback;

/**
 * 简化 Callback 的错误回调
 * </p>
 * Created by Billin on 2017/5/12.
 */
public abstract class SuccessResultCallback<D> implements BaseResultCallback<D> {

    private BaseResultCallback errorCallback;

    public SuccessResultCallback(BaseResultCallback errorCallback) {
        this.errorCallback = errorCallback;
    }

    @Override
    public void onFail(String msg) {
        errorCallback.onFail(msg);
    }
}
