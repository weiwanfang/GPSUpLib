//package com.example.upgpsinfolibrary.Receiver;
//
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//
//import com.example.upgpsinfolibrary.gpstest.ConstantValues;
//import com.example.upgpsinfolibrary.gpstest.SharedPreferencesUtils;
//import com.example.upgpsinfolibrary.gpstest.UpSDK;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.Iterator;
//
//import cn.jpush.android.api.JPushInterface;
//
///**
// * 自定义接收器
// * <p>
// * 如果不定义这个 Receiver，则：
// * 1) 默认用户会打开主界面
// * 2) 接收不到自定义消息
// */
//public class JPushReceiver extends BroadcastReceiver {
//    private static final String TAG = "极光推送";
//    private String uid = "uid";
//    private String sid = "sid";
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        try {
//            Bundle bundle = intent.getExtras();
//            Logger.d(TAG, "[JPushReceiver] onReceive - " + intent.getAction() + ", extras: " + printBundle(bundle));
//
//            if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
//                String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
//                Logger.d(TAG, "[JPushReceiver] 接收Registration Id : " + regId);
//                //send the Registration Id to your server...
//
//
//            } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
//                Logger.d(TAG, "[JPushReceiver] 接收到推送下来的自定义消息: " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
////                processCustomMessage(context, bundle);
//
//                uid = String.valueOf(SharedPreferencesUtils.getParam(context, ConstantValues.UID, "6666"));
//                sid = String.valueOf(SharedPreferencesUtils.getParam(context, ConstantValues.SID, "7777"));
//
//                Logger.d(TAG, "极光uid: " + uid + " sid: " + sid);
//                UpSDK.UpSDKinit(context, uid, sid);
//
//            } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
//                Logger.d(TAG, "[JPushReceiver] 接收到推送下来的通知");
//                String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
//                Logger.d(TAG, "[JPushReceiver] 接收Registration Id : " + regId);
//                //send the Registration Id to your server...
//                int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
//                Logger.d(TAG, "[JPushReceiver] 接收到推送下来的通知的ID: " + notifactionId);
//
//
//                uid = String.valueOf(SharedPreferencesUtils.getParam(context, ConstantValues.UID, "6666"));
//                sid = String.valueOf(SharedPreferencesUtils.getParam(context, ConstantValues.SID, "7777"));
//
//                Logger.d(TAG, "极光uid: " + uid + " sid: " + sid);
//                UpSDK.UpSDKinit(context, uid, sid);
//
////                receivingNotification(context,bundle);
//
//            } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
//                Logger.d(TAG, "[JPushReceiver] 用户点击打开了通知");
////                //打开自定义的Activity
////                Intent i = new Intent(context, MainActivity.class);
//////                i.putExtras(bundle);
////                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                context.startActivity(i);
//
//            } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
//                Logger.d(TAG, "[JPushReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
//                //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
//
//            } else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
//                boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
//                Logger.w(TAG, "[JPushReceiver]" + intent.getAction() + " connected state change to " + connected);
//            } else {
//                Logger.d(TAG, "[JPushReceiver] Unhandled intent - " + intent.getAction());
//            }
//        } catch (Exception e) {
//
//        }
//
//    }
//
//    // 打印所有的 intent extra 数据
//    private static String printBundle(Bundle bundle) {
//        StringBuilder sb = new StringBuilder();
//        for (String key : bundle.keySet()) {
//            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
//                sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
//            } else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
//                sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
//            } else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
//                if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
//                    Logger.i(TAG, "This message has no Extra data");
//                    continue;
//                }
//
//                try {
//                    JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
//                    Iterator<String> it = json.keys();
//
//                    while (it.hasNext()) {
//                        String myKey = it.next().toString();
//                        sb.append("\nkey:" + key + ", value: [" +
//                                myKey + " - " + json.optString(myKey) + "]");
//                    }
//                } catch (JSONException e) {
//                    Logger.e(TAG, "Get message extra JSON error!");
//                }
//
//            } else {
//                sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
//            }
//        }
//        return sb.toString();
//    }
//
//    //send msg to JPushMainActivity
//    private void processCustomMessage(Context context, Bundle bundle) {
////        if (JPushMainActivity.isForeground) {
////            String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
////            String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
////            Intent msgIntent = new Intent(JPushMainActivity.MESSAGE_RECEIVED_ACTION);
////            msgIntent.putExtra(JPushMainActivity.KEY_MESSAGE, message);
////            if (!ExampleUtil.isEmpty(extras)) {
////                try {
////                    JSONObject extraJson = new JSONObject(extras);
////                    if (extraJson.length() > 0) {
////                        msgIntent.putExtra(JPushMainActivity.KEY_EXTRAS, extras);
////                    }
////                } catch (JSONException e) {
////
////                }
////
////            }
////            LocalBroadcastManager.getInstance(context).sendBroadcast(msgIntent);
////        }
//    }
//
//    private NotificationManager nm;
//
//    private void receivingNotification(Context context, Bundle bundle) {
//        String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
//        Logger.d(TAG, " title : " + title);
//        String message = bundle.getString(JPushInterface.EXTRA_ALERT);
//        Logger.d(TAG, "message : " + message);
//        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
//        Logger.d(TAG, "extras : " + extras);
//    }
//
//    private void openNotification(Context context, Bundle bundle) {
//        String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
//        String myValue = "";
//        try {
//            JSONObject extrasJson = new JSONObject(extras);
//            myValue = extrasJson.optString("myKey");
//        } catch (Exception e) {
//            Logger.i(TAG, "Unexpected: extras is not a valid json");
//            return;
//        }
//    }
//
//
//}
