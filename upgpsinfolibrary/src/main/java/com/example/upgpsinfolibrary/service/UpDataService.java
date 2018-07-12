package com.example.upgpsinfolibrary.service;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.example.upgpsinfolibrary.R;
import com.example.upgpsinfolibrary.gpstest.ConstantValues;
import com.example.upgpsinfolibrary.gpstest.DateFormatterTool;
import com.example.upgpsinfolibrary.gpstest.MDebug;
import com.example.upgpsinfolibrary.gpstest.NetworkUtil;
import com.example.upgpsinfolibrary.gpstest.SharedPreferencesUtils;
import com.example.upgpsinfolibrary.gpstest.Utils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;


/**
 * Created by weiwanfang on 2017/6/8 17:23
 * mailbox: weiwanfang@foxmail.com
 * description: 数据上传
 * version:
 * update:
 */
public class UpDataService extends BaseService implements MediaPlayer.OnCompletionListener {
    private String permissionInfo;
    private static final String TAG = "UpDataService";
    private static final String TAG1 = "返回结果";
    private Context mContext;
    private final int SDK_PERMISSION_REQUEST = 127;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = new AMapLocationClientOption();
    private String uid = "UID获取失败";
    private String sid = "SID获取失败";
    private PowerManager.WakeLock wakeLock = null;

    private boolean mPausePlay = false;//控制是否播放音频
    private MediaPlayer mediaPlayer;
    private Handler mHandler = new Handler();


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = UpDataService.this.getApplicationContext();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, UpDataService.class.getName());
        wakeLock.acquire();
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        MDebug.debug(TAG, "----onStartCommand---");
        initLocation();
        getConfig();
//        upGPSinfo();
//        startLocation();
        try {
            uid = intent.getStringExtra(ConstantValues.UID);
            sid = intent.getStringExtra(ConstantValues.SID);

//            SharedPreferencesUtils.setParam(mContext, ConstantValues.UID, uid);
//            SharedPreferencesUtils.setParam(mContext, ConstantValues.SID, sid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (uid == null || sid == null) {
            uid = String.valueOf(SharedPreferencesUtils.getParam(mContext, ConstantValues.UID, "uid"));
            sid = String.valueOf(SharedPreferencesUtils.getParam(mContext, ConstantValues.SID, "sid"));
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.test_music);
            mediaPlayer.setVolume(0f, 0f);
            mediaPlayer.setOnCompletionListener(this);
        }
        play();
        return START_STICKY;

//        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 播放音频
     * 亮屏：播放保活
     * 锁屏：已连接，播音乐；未连接，不播放
     */
    private void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && !mPausePlay) {
            mediaPlayer.start();
        }
    }

    /**
     * 停止播放
     */
    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        mPausePlay = true;
    }

    //播放完成
    @Override
    public void onCompletion(MediaPlayer mp) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                play();
            }
        }, 20 * 1000);
    }


    private HeartbeatCountDownTimer heartbeatCountDownTimer;//心跳倒计时


    public class HeartbeatCountDownTimer extends CountDownTimer {

        public HeartbeatCountDownTimer(long millisInFuture) {
            super(millisInFuture * 1000 * 60, 1000);
        }

        @Override
        public void onFinish() {
            heartbeatCountDownTimer = null;
            getConfig();

        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.i(TAG, "-------心跳倒计时：   " + millisUntilFinished / 1000);
        }
    }


    private void getConfig() {
        if (NetworkUtil.isNetAvailable(mContext)) {
            RequestParams params = new RequestParams();
            params.addQueryStringParameter("uid", uid);
            params.addQueryStringParameter("sid", sid);
//            params.addQueryStringParameter("registerID", JPushInterface.getRegistrationID(mContext));
            getData(mContext, 1000, "http://132.232.17.139:8360/api/andriodconfig", params, HttpRequest.HttpMethod.GET);
        } else {
            Toast.makeText(mContext, "服务器异常", Toast.LENGTH_LONG);
        }
    }


    private int verid;//配置版本号
    private int heartbeat_interval;//心跳（获取配置）时间间隔，单位为分。
    private int idle_interval;// 闲时间隔，即非排班时间点其它时间上报位置时间间隔，单位为分。
    private int scheduling_ranges;//排班时间范围
    private int scheduling_interval;//排班时间点范围内上报位置时间间隔，单位为分，小于scheduling_ranges。

    private String report_starttime;//每天开始上报地理位置的时间
    private String report_endtime;//  每天结束上报地理位置的时间。
    private String scheduling_time;//排班时间

    private UpDataCountDownTimer upDataCountDownTimer;//数据上传倒计时


    private String nowTime;

    public class UpDataCountDownTimer extends CountDownTimer {

        public UpDataCountDownTimer(long millisInFuture) {
            super(millisInFuture * 1000, 1000);
        }

        @Override
        public void onFinish() {
            this.start();
        }

        @Override
        public void onTick(long millisUntilFinished) {

            nowTime = DateFormatterTool.getNow();
            Log.i("nowTime", nowTime);
            try {

                Date nowDate = DateFormatterTool.toDate(nowTime);
                Date reportStarttime = DateFormatterTool.toDate(report_starttime);
                Date reportEndtime = DateFormatterTool.toDate(report_endtime);
                boolean isbelong = DateFormatterTool.belongCalendar(nowDate, reportStarttime, reportEndtime);

                Log.i("isbelong", "是否在上报时间范围内： " + isbelong);
                if (isbelong) {
                    //在上报时间内

                    JSONArray jsonArray = new JSONArray(scheduling_time);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Date schedulingTime = DateFormatterTool.toDate(jsonArray.getString(i));//排班时间
                        boolean isbelongSchedulingTime = DateFormatterTool.belongCalendar(nowDate,
                                new Date(schedulingTime.getTime() - scheduling_ranges * 60 * 1000),
                                new Date(schedulingTime.getTime() + scheduling_ranges * 60 * 1000));//是否在排班时间范围内

                        Log.i("isbelongSchedulingTime", "是否在排班时间范围内:  " + isbelongSchedulingTime);
                        if (isbelongSchedulingTime) {
                            //在排班时间范围内，每隔scheduling_interval分钟上报一次
                            if (schedulingTimeCountDownTimer == null) {
                                schedulingTimeCountDownTimer = new SchedulingTimeCountDownTimer(scheduling_interval);
                                schedulingTimeCountDownTimer.start();
                            }
                        } else {
                            //闲时间隔，即非排班时间点其它时间上报位置时间间隔，单位为分。
                            if (schedulingTimeCountDownTimer == null) {
                                schedulingTimeCountDownTimer = new SchedulingTimeCountDownTimer(idle_interval);
                                schedulingTimeCountDownTimer.start();
                            }
                        }
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private SchedulingTimeCountDownTimer schedulingTimeCountDownTimer;

    public class SchedulingTimeCountDownTimer extends CountDownTimer {

        public SchedulingTimeCountDownTimer(long millisInFuture) {
            super(millisInFuture * 1000 * 60, 1000);
            startLocation();
        }

        @Override
        public void onFinish() {
            Log.i(TAG, "-------开始上报------");
            schedulingTimeCountDownTimer = null;
            startLocation();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Log.i(TAG, "-------上报倒计时------：  " + millisUntilFinished / 1000);
        }
    }

    /*
    [{
                "verid": 3,
                "heartbeat_interval": 9,
                "idle_interval": 16,
                "scheduling_ranges": 15,
                "scheduling_interval": 2,

                "report_starttime": "06:00:00",
                "report_endtime": "20:00:00",
                "scheduling_time": ["07:30:00", "13:00:00", "18:30:00"]
    }]*/

    @Override
    public void handleMsg(Message msg) {
        String json = msg.getData().getString("json");
        switch (msg.what) {
            case 1000:
                Log.i(TAG1, "getConfig: " + json);
                try {
                    JSONArray jsonArray = new JSONArray(json);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    verid = jsonObject.getInt("verid");
                    heartbeat_interval = jsonObject.getInt("heartbeat_interval");
                    idle_interval = jsonObject.getInt("idle_interval");
                    scheduling_ranges = jsonObject.getInt("scheduling_ranges");
                    scheduling_interval = jsonObject.getInt("scheduling_interval");

                    report_starttime = jsonObject.getString("report_starttime");
                    report_endtime = jsonObject.getString("report_endtime");
                    scheduling_time = jsonObject.getString("scheduling_time");

                    if (heartbeatCountDownTimer == null) {
                        Log.i(TAG, "heartbeat_interval: " + heartbeat_interval);
                        heartbeatCountDownTimer = new HeartbeatCountDownTimer(heartbeat_interval);
                        heartbeatCountDownTimer.start();
                    }

                    if (upDataCountDownTimer == null) {
                        upDataCountDownTimer = new UpDataCountDownTimer(1);
                        upDataCountDownTimer.start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 1001:
                Log.i(TAG1, "upGPSinfo: " + json);
                break;
            case 10000:
                Log.i(TAG1, "服务器异常");
                break;
            default:
                break;
        }

    }

    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }


    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                String result = Utils.getLocationStr(loc);
                Log.i(TAG, "定位结果: " + result);
                latitude = loc.getLatitude() + "";//纬度
                longitude = loc.getLongitude() + "";//经度
                radius = loc.getAccuracy() + "";


//                Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();

                upGPSinfo(uid, sid);
                //构造一个示例坐标，第一个参数是纬度，第二个参数是经度
                DPoint examplePoint = new DPoint(loc.getLatitude(), loc.getLongitude());


//                try {
//                    //初始化坐标转换类
//                    CoordinateConverter converter = new CoordinateConverter(
//                            getApplicationContext());
//                    /**
//                     * 设置坐标来源,这里使用百度坐标作为示例
//                     * 可选的来源包括：
//                     * <li>CoordType.BAIDU ： 百度坐标
//                     * <li>CoordType.MAPBAR ： 图吧坐标
//                     * <li>CoordType.MAPABC ： 图盟坐标
//                     * <li>CoordType.SOSOMAP ： 搜搜坐标
//                     * <li>CoordType.ALIYUN ： 阿里云坐标
//                     * <li>CoordType.GOOGLE ： 谷歌坐标
//                     * <li>CoordType.GPS ： GPS坐标
//                     */
//                    converter.from(CoordinateConverter.CoordType.BAIDU);
//                    //设置需要转换的坐标
//                    converter.coord(examplePoint);
//                    //转换成高德坐标
//                    DPoint destPoint = converter.convert();
//                    if (null != destPoint) {
//                        Log.i(TAG, "转换后坐标(经度、纬度):" + destPoint.getLongitude() + "," + destPoint.getLatitude());
//                        latitude = destPoint.getLatitude() + "";//纬度
//                        longitude = destPoint.getLongitude() + "";//经度
//                    } else {
//                        Log.i(TAG, "坐标转换失败");
//                    }
//                } catch (Exception e) {
//                    Log.i(TAG, "坐标转换失败");
//                    e.printStackTrace();
//                }
//
//                upGPSinfo();
            } else {
                Log.i(TAG, "定位失败，loc is null");
            }


        }
    };

    private String latitude;
    private String longitude;
    private String radius;

    private void upGPSinfo(String uid, String sid) {
        if (NetworkUtil.isNetAvailable(mContext)) {
            Log.i(TAG1, "开始上传GPS");
//            RequestParams params = new RequestParams();
//            params.addBodyParameter("report_time", nowTime);// 上报位置时间
//            params.addBodyParameter("latitude", latitude);// 纬度信息。
//            params.addBodyParameter("longitude", longitude);//经度信息。
//            params.addBodyParameter("radius", radius);//定位精度。
//            params.addBodyParameter("coorType", "BAIDU");//坐标类型。
//            params.addBodyParameter("errorCode", "0");//定位类型、定位错误返回码及描述。
//
//            params.addQueryStringParameter("uid", uid);
//            params.addQueryStringParameter("sid", sid);
//            getData(mContext, 1001, "http://132.232.17.139:8360/api/andriodpos", params, HttpRequest.HttpMethod.POST);

            JSONObject finaljson = new JSONObject();
            try {
                finaljson.put("report_time", nowTime);// 上报位置时间
                finaljson.put("latitude", latitude);// 纬度信息。
                finaljson.put("longitude", longitude);//经度信息。
                finaljson.put("radius", radius);//定位精度。
                finaljson.put("coorType", "BAIDU");//坐标类型。
                finaljson.put("errorCode", "0");//定位类型、定位错误返回码及描述。
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.i("UpActivity", "finaljson: " + finaljson.toString());
            OkGo.<LzyResponse<ServerModel>>post("http://132.232.17.139:8360/api/andriodpos?uid=" + uid + "&sid=" + sid)//
                    .tag(this)//

//                .params("uid", "wwwww")//
//                .params("sid", "cccc")//
//                .params("param1", "paramValue1")//  这里不要使用params，upJson 与 params 是互斥的，只有 upJson 的数据会被上传
                    .upJson(finaljson)//
                    .execute(new DialogCallback<LzyResponse<ServerModel>>(mContext) {
                        @Override
                        public void onSuccess(Response<LzyResponse<ServerModel>> response) {
                        }

                        @Override
                        public void onError(Response<LzyResponse<ServerModel>> response) {
                        }
                    });


        } else {
            Toast.makeText(mContext, "服务器异常", Toast.LENGTH_LONG);
        }


    }


    // 根据控件的选择，重新设置定位参数
    private void resetOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(true);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(true);
        // 设置是否单次定位
        locationOption.setOnceLocation(true);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
        locationOption.setOnceLocationLatest(true);
        //设置是否使用传感器
        locationOption.setSensorEnable(true);
        //设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
        locationOption.setInterval(Long.valueOf(2000));
        // 设置网络请求超时时间
        locationOption.setHttpTimeOut(Long.valueOf(30000));
        locationOption.setMockEnable(true);//设置是否允许模拟位置,默认为true，允许模拟位置
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

    }


    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(true);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(true);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }


    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
        //根据控件的选择，重新设置定位参数
        resetOption();
        // 设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

}
