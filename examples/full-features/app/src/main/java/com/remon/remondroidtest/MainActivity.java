package com.remon.remondroidtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.remon.remondroid.Config;
import com.remon.remondroid.PercentFrameLayout;
import com.remon.remondroid.Remon;
import com.remon.remondroid.RemonObserver;
import com.remon.remondroid.RemonState;
import com.remon.remondroid.util.Logger;

import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;
    private Remon remon = null;
    private SharedPreferences pref = null;

    private EditText editText1 = (EditText) findViewById(R.id.channelNameInputBox);
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
        final EditText logbox = (EditText) findViewById(R.id.logBox);
        localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        remoteRender = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);

        Button settingsButton = (Button)findViewById(R.id.setupButton);
        editText1.setText("demo"+ new Random().nextInt(99));

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        settingsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Init Button click event
        findViewById(R.id.initButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Config config = new Config();
                config.setServiceId("simpleapp");
                config.setKey("e3ee6933a7c88446ba196b2c6eeca6762c3fdceaa6019f03");
                config.setConfig(pref);
                config.setLocalView(localRender);
                config.setRemoteView(remoteRender);
                remon = new Remon(MainActivity.this, config, new AppObserver(MainActivity.this,remon));
                Logger.i("MainActivity","remon is created");

            }
        });

        findViewById(R.id.connectButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence chId = ((EditText) findViewById(R.id.channelNameInputBox)).getText();
                if (chId == null )chId = "demo";
                try {
                    remon.connectChannel(chId.toString());
                    updateVideoViewForInit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        //close btn click event
        findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remon.close();
            }
        });

    }

    public void setStatus(final RemonState state){

        this.runOnUiThread(new Runnable() {
            public void run() {
                String stat = "State:"+ state.getState();
                EditText et = (EditText)findViewById(R.id.statBox);
                if (remon.getRemonContext()!=null && remon.getRemonContext().getChannel()!=null && remon.getRemonContext().getChannel().getId()!=null)
                    stat += "\nchId:"+remon.getRemonContext().getChannel().getId();
                et.setText(stat+"\n"+et.getText());
            }
        });

    }
    public void addMessage(final String msg){
        this.runOnUiThread(new Runnable() {
            public void run() {
                EditText et = (EditText)findViewById(R.id.statBox);

                et.setText(msg+"\n"+et.getText());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (remon!=null){
            remon.startLocalVideo();
        }
    }

    public Remon getRemon(){
        return remon;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (remon!=null)remon.pauseLocalVideo();
    }

    public void updateVideoViewForInit(){
        remoteRenderLayout.setPosition(50,50,50,50);
        localRenderLayout.setPosition(0,0,80,80);
        localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        localRender.setZOrderMediaOverlay(true);
        localRender.setMirror(false);
        localRender.requestLayout();
        remoteRender.requestLayout();
    }

    @Override
    protected void onDestroy(){
        if (remon!=null)
            remon.close();
        finish();
        super.onDestroy();
    }

    public void setChannelIdText(String channelIdText) {
        ((EditText)findViewById(R.id.channelNameInputBox)).setText(channelIdText);
    }

    @SuppressLint("NewApi")
    private void checkPermission(String[] permissions) {
        requestPermissions(permissions, 100);
    }
}
