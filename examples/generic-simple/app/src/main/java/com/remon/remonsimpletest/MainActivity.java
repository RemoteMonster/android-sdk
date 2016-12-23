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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            checkPermission(MANDATORY_PERMISSIONS);
        }

        Config config = new com.remon.remondroid.Config();
        config.setLocalView((SurfaceViewRenderer) findViewById(R.id.local_video_view));
        config.setRemoteView((SurfaceViewRenderer) findViewById(R.id.remote_video_view));


        remon = new Remon(MainActivity.this, config, new RemonObserver(){ });

        findViewById(R.id.connectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    remon.connectChannel(("simpleRemon"));


            }
        });

    }

    @Override
    protected void onDestroy() {
        remon.close();
        super.onDestroy();
    }

    public void updateVideoViewForInit(){
        SurfaceViewRenderer localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        PercentFrameLayout localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        SurfaceViewRenderer remoteRender = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        PercentFrameLayout remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
        remoteRenderLayout.setPosition(0,0,100,100);
        localRenderLayout.setPosition(60,60,20,20);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localRender.setZOrderMediaOverlay(true);

        localRender.setMirror(false);
        remoteRender.requestLayout();
        localRender.requestLayout();

    }

    @SuppressLint("NewApi")
    private void checkPermission(String[] permissions) {
        requestPermissions(permissions, 100);
    }
}
