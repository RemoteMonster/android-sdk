package com.remotemonster.sdktest.sample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.remon.sdktest.R;

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
    Button btnRemonCast;
    Button btnRemonCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router);
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermission(MANDATORY_PERMISSIONS);
        }

        btnRemonCast = (Button) findViewById(R.id.btnRemonCast);
        btnRemonCall = (Button) findViewById(R.id.btnRemonCall);


        btnRemonCall.setOnClickListener(v -> {
            Intent intent = new Intent(RouterActivity.this, ListActivity.class);
            intent.putExtra("remonType", 0);
            startActivity(intent);
        });

        btnRemonCast.setOnClickListener(v -> {
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
