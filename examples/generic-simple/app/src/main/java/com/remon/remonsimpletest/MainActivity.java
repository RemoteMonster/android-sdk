package com.remon.remonsimpletest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.remon.remondroid.Config;
import com.remon.remondroid.PercentFrameLayout;
import com.remon.remondroid.Remon;
import com.remon.remondroid.RemonException;
import com.remon.remondroid.RemonObserver;
import com.remon.remondroid.RemonState;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

public class MainActivity extends AppCompatActivity {
    Remon remon = null;

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
    private SurfaceViewRenderer localRender = null;
    private PercentFrameLayout localRenderLayout = null;
    private SurfaceViewRenderer remoteRender = null;
    private PercentFrameLayout remoteRenderLayout = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            checkPermission(MANDATORY_PERMISSIONS);
        }
        localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        remoteRender = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);

        // key와 serviceid를 설정하지 않았기에 WIFI기반에서만 실행됨. site에서 키발급 신청 후 lte등에서 접속됨
        // You can't connect on this config cause you don't have key and serviceId. please request to homepage
        Config config = new com.remon.remondroid.Config();
        config.setServiceId("simpleapp");
        config.setKey("e3ee6933a7c88446ba196b2c6eeca6762c3fdceaa6019f03");
        config.setLocalView(localRender);
        config.setRemoteView(remoteRender);


        try {
            remon = new Remon(MainActivity.this, config, new RemonObserver(){ });
        } catch (RemonException e) {
            e.printStackTrace();
        }

        findViewById(R.id.connectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    remon.connectChannel(("simpleRemon"));
                    updateVideoViewForInit();

            }
        });
        findViewById(R.id.disconnectButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                remon.close();
            }
        });

        localRender.setZOrderMediaOverlay(true);

    }

    @Override
    protected void onDestroy() {
        remon.close();
        super.onDestroy();
    }

    public void updateVideoViewForInit(){

        remoteRenderLayout.setPosition(0,0,100,100);
        localRenderLayout.setPosition(60,60,20,20);
        remoteRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);


        localRender.setMirror(false);
        remoteRender.requestLayout();
        localRender.requestLayout();

    }

    @SuppressLint("NewApi")
    private void checkPermission(String[] permissions) {
        requestPermissions(permissions, 100);
    }
}
