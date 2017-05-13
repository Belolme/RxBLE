package com.billin.www.rxble.ble.callback;

/**
 * 用于数据回传的基本回调接口
 * <p/>
 * Created by Billin on 2017/3/9.
 */
public interface BaseResultCallback<D> {

    /**
     * 成功拿到数据
     *
     * @param data 回传的数据
     */
    void onSuccess(D data);

    /**
     * 操作失败
     *
     * @param msg 失败的返回的异常信息
     */
    void onFail(String msg);
}
