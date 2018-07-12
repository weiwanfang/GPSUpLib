package com.example.upgpsinfolibrary.gpstest;

import android.content.Context;
import android.content.Intent;

import com.example.upgpsinfolibrary.BuildConfig;
import com.example.upgpsinfolibrary.service.TraceServiceImpl;
import com.example.upgpsinfolibrary.wakeup.assistantReceiver;
import com.revenco.daemon.DaemonManager;
import com.revenco.daemon.java.services.DaemonEnv;

/**
 * Created by weiwanfang on on 2018/6/27 17:25
 * mailbox: weiwanfang@foxmail.com
 * description:
 * update:
 * version:
 */
public class UpSDK {

    public static void UpSDKinit(Context context, String uid, String sid) {
//        Intent mUpDateServiceIntent = new Intent(context, UpDataService.class);
//        mUpDateServiceIntent.putExtra(ConstantValues.UID, uid);
//        mUpDateServiceIntent.putExtra(ConstantValues.SID, sid);
//        context.startService(mUpDateServiceIntent);

        SharedPreferencesUtils.setParam(context, ConstantValues.UID, uid);
        SharedPreferencesUtils.setParam(context, ConstantValues.SID, sid);

        //主要的业务逻辑进程
        String processName = "com.revenco.app:business";
        String serviceName = TraceServiceImpl.class.getCanonicalName();
        String receiveName = assistantReceiver.class.getCanonicalName();
        DaemonManager.INSTANCE.init(context, processName, serviceName, receiveName);
        if (BuildConfig.DEBUG)
            //Debug模式开启控制台LOG日志，部分唤醒日志则会记录到SDCard，方便观察app如何被唤醒的日志
            DaemonManager.INSTANCE.initLogFile(context);
        //配置
        DaemonEnv.initialize(context, TraceServiceImpl.class, DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        try {
            //启动
            Intent mUpDateServiceIntent = new Intent(context, TraceServiceImpl.class);
            mUpDateServiceIntent.putExtra(ConstantValues.UID, uid);
            mUpDateServiceIntent.putExtra(ConstantValues.SID, sid);
            context.startService(mUpDateServiceIntent);
        } catch (Exception e) {
        }


    }




}
