package com.example.upgpsinfolibrary.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

/**
 * Created by weiwanfang on  2017/4/6 10:59
 * mailbox: weiwanfang@foxmail.com
 * description:
 * update:
 * version:
 */
@SuppressLint("HandlerLeak")
public abstract class BaseService extends Service implements RequestListener {

    public void getData(Context mContext, int tag, String url, RequestParams params, HttpMethod method) {
        BaseAsyncRunner.request(tag, url, params, method, this);


    }

    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            handleMsg(msg);
        }
    };

    @Override
    public void onComplete(int tag, String json) {
        Message msg = mHandler.obtainMessage();
        msg.what = tag;
        Bundle data = new Bundle();
        data.putString("json", json);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }

    @Override
    public void onException(String json) {
        Message msg = mHandler.obtainMessage();
        msg.what = 10000;
        Bundle data = new Bundle();
        data.putString("json", json);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }

    public abstract void handleMsg(Message msg);

}
