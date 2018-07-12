package com.example.upgpsinfolibrary.service;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"unchecked", "rawtypes"})
public class BaseAsyncRunner {
    public static void request(final int tag, final String url, final RequestParams params, final HttpMethod method, final RequestListener listener) {
        ExecutorService es = Executors.newFixedThreadPool(10);
        es.submit(new Runnable() {
            @Override
            public void run() {
                HttpUtils http = new HttpUtils();
                http.send(method, url, params, new RequestCallBack() {
                    @Override
                    public void onSuccess(ResponseInfo responseInfo) {
                        listener.onComplete(tag, responseInfo.result.toString());
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        listener.onComplete(10000, msg);
                    }
                });
            }
        });
    }

}
