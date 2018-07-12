package com.example.weiwanfang.gpstest;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.upgpsinfolibrary.gpstest.UpSDK;
import com.revenco.daemon.java.services.IntentWrapper;

import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private String permissionInfo;
    private final int SDK_PERMISSION_REQUEST = 127;
    private TextView locationResultTxt;
    private Button startLocationBtn;
    private Context mContext;


    private EditText uidEdit;
    private EditText sidEdit;
    private Button startBtn;
    private Button baiBtn;
    private String uid;
    private String sid;
    public final static String UID = "uid";
    public final static String SID = "sid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        getPersimmions();

        uidEdit = (EditText) findViewById(R.id.uidEdit);
        sidEdit = (EditText) findViewById(R.id.sidEdit);
        startBtn = (Button) findViewById(R.id.startBtn);
        baiBtn = (Button) findViewById(R.id.baiBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uid = uidEdit.getText().toString().trim();
                sid = sidEdit.getText().toString().trim();
                if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(sid)) {
                    UpSDK.UpSDKinit(mContext, uid, sid);
                    Toast.makeText(mContext, "已启动", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "请输入参数", Toast.LENGTH_LONG).show();
                }
            }
        });
        baiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentWrapper.whiteListMatters(MainActivity.this, "为了确保APP接收消息的实时性，将添加到白名单。");
            }
        });

        ignoreBatteryOptimization(MainActivity.this);

    }
    /**
     * 忽略电池优化
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void ignoreBatteryOptimization(Activity activity) {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
        //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
        if(!hasIgnored) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:"+activity.getPackageName()));
            startActivity(intent);
        }
    }



    @TargetApi(23)
    private void getPersimmions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> permissions = new ArrayList<String>();
            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
             */
            // 读写权限
            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }

        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


}
