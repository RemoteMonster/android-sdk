package com.remotemonster.sdktest.sample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.remon.sdktest.R;
import com.remon.sdktest.databinding.ActivityRouterBinding;

public class RouterActivity extends AppCompatActivity {
    public static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.INTERNET",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.CHANGE_WIFI_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.READ_PHONE_STATE",
            "android.permission.BLUETOOTH",
            "android.permission.BLUETOOTH_ADMIN",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };
    private ActivityRouterBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_router);

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission(MANDATORY_PERMISSIONS);
        }

        mBinding.btnRemonCall.setOnClickListener(v -> {
            Intent intent = new Intent(RouterActivity.this, ListActivity.class);
            intent.putExtra("remonType", 0);
            startActivity(intent);
        });

        mBinding.btnRemonCast.setOnClickListener(v -> {
            Intent intent = new Intent(RouterActivity.this, ListActivity.class);
            intent.putExtra("remonType", 1);
            startActivity(intent);
        });
    }

    @SuppressLint("NewApi")
    private void checkPermission(String[] permissions) {
        requestPermissions(permissions, 100);
    }
}
