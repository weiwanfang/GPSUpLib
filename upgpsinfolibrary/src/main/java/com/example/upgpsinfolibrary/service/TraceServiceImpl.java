package com.example.upgpsinfolibrary.service;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.upgpsinfolibrary.gpstest.ConstantValues;
import com.example.upgpsinfolibrary.gpstest.DateFormatterTool;
import com.example.upgpsinfolibrary.gpstest.SharedPreferencesUtils;
import com.revenco.daemon.java.services.AbsWorkService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.reactivex.disposables.Disposable;

/**
 * 这个是核心的业务服务
 */
public class TraceServiceImpl extends AbsWorkService {
    //是否 任务完成, 不再需要服务运行?
    public static boolean sShouldStopService;
    public static Disposable sDisposable;
    private String uid = "1111";
    private String sid = "2222";
    private Context mContext;

    public static void stopService() {
        //我们现在不再需要服务运行了, 将标志位置为 true
        sShouldStopService = true;
        //取消对任务的订阅
        if (sDisposable != null)
            sDisposable.dispose();
        //取消 Job / Alarm / Subscription
        cancelJobAlarmSub();
    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     *
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean shouldStopService(Intent intent, int flags, int startId) {
        return sShouldStopService;
    }

    @Override
    public void startWork(Intent intent, int flags, int startId) {

        mContext = TraceServiceImpl.this.getApplicationContext();
        try {
            uid = intent.getStringExtra(ConstantValues.UID);
            sid = intent.getStringExtra(ConstantValues.SID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (uid == null || sid == null) {
            uid = String.valueOf(SharedPreferencesUtils.getParam(mContext, ConstantValues.UID, "uid"));
            sid = String.valueOf(SharedPreferencesUtils.getParam(mContext, ConstantValues.SID, "sid"));
        }



        Intent mUpDateServiceIntent = new Intent(mContext, UpDataService.class);
        mUpDateServiceIntent.putExtra(ConstantValues.UID, uid);
        mUpDateServiceIntent.putExtra(ConstantValues.SID, sid);
        mContext.startService(mUpDateServiceIntent);

        if (schedulingTimeCountDownTimer == null) {
            schedulingTimeCountDownTimer = new SchedulingTimeCountDownTimer(5);
            schedulingTimeCountDownTimer.start();
        }


    }

    private SchedulingTimeCountDownTimer schedulingTimeCountDownTimer;

    public class SchedulingTimeCountDownTimer extends CountDownTimer {

        public SchedulingTimeCountDownTimer(long millisInFuture) {
            super(millisInFuture * 1000, 1000);
        }

        @Override
        public void onFinish() {
            start();
            String string = "uid=" + uid + " sid=" + sid + " time= " + DateFormatterTool.Now(0);
            Log.i("保存数据到磁盘", string);
            writeFileToSDCard(string.getBytes(), "1aSDK保活测试", "保活测试.txt", true, true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }


    @Override
    public void stopWork(Intent intent, int flags, int startId) {
        stopService();
    }

    /**
     * 任务是否正在运行?
     *
     * @return 任务正在运行, true; 任务当前不在运行, false;
     */
    @Override
    public Boolean isWorkRunning(Intent intent, int flags, int startId) {
        //若还没有取消订阅, 就说明任务仍在运行.
        return sDisposable != null && !sDisposable.isDisposed();
    }

    @Override
    public IBinder onBind(Intent intent, Void v) {
        return null;
    }

    @Override
    public void onServiceKilled(Intent rootIntent) {
        System.out.println("保存数据到磁盘。");
    }


    public static void getString(String str) {
        String filePath = null;
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (hasSDCard) {
            filePath = Environment.getExternalStorageDirectory().toString() + File.separator + "hello.txt";
        } else
            filePath = Environment.getDownloadCacheDirectory().toString() + File.separator + "hello.txt";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(str.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized static void writeFileToSDCard(@NonNull final byte[] buffer, @Nullable final String folder,
                                                      @Nullable final String fileName, final boolean append, final boolean autoLine) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean sdCardExist = Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED);
                String folderPath = "";
                if (sdCardExist) {
                    //TextUtils为android自带的帮助类
                    if (TextUtils.isEmpty(folder)) {
                        //如果folder为空，则直接保存在sd卡的根目录
                        folderPath = Environment.getExternalStorageDirectory()
                                + File.separator;
                    } else {
                        folderPath = Environment.getExternalStorageDirectory()
                                + File.separator + folder + File.separator;
                    }
                } else {
                    return;
                }

                File fileDir = new File(folderPath);
                if (!fileDir.exists()) {
                    if (!fileDir.mkdirs()) {
                        return;
                    }
                }
                File file;
                //判断文件名是否为空
                if (TextUtils.isEmpty(fileName)) {
                    file = new File(folderPath + "app_log.txt");
                } else {
                    file = new File(folderPath + fileName);
                }
                RandomAccessFile raf = null;
                FileOutputStream out = null;
                try {
                    if (append) {
                        //如果为追加则在原来的基础上继续写文件
                        raf = new RandomAccessFile(file, "rw");
                        raf.seek(file.length());
                        raf.write(buffer);
                        if (autoLine) {
                            raf.write("\n".getBytes());
                        }
                    } else {
                        //重写文件，覆盖掉原来的数据
                        out = new FileOutputStream(file);
                        out.write(buffer);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (raf != null) {
                            raf.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
