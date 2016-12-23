package com.remon.remondroidtest;

import android.Manifest;
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

public class MainActivity extends AppCompatActivity {
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;
    private Remon remon = null;
    private SharedPreferences pref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionRequest(Manifest.permission.RECORD_AUDIO);
        permissionRequest(Manifest.permission.CAMERA);
        permissionRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        final EditText logbox = (EditText) findViewById(R.id.logBox);
        localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        remoteRender = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);

        Button settingsButton = (Button)findViewById(R.id.setupButton);

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
        // option button click event
        findViewById(R.id.optButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Config config = new Config();
                config.setConfig(pref);
                config.setLocalView(localRender);
                config.setRemoteView(remoteRender);
                if (remon !=null)return;
                remon = new Remon(MainActivity.this, config, new RemonObserver(){
                    @Override
                    public void onStateChange(RemonState state) {
                        super.onStateChange(state);
                        setStatus(state);
                        if (state==RemonState.INIT){
                            remon.connectChannel("demo");
                            updateVideoViewForInit();

                        }
                    }
                });

            }
        });
    }

    public void setStatus(final RemonState state){

        this.runOnUiThread(new Runnable() {
            public void run() {
                String stat = "State:"+ state.getState();
                if (remon.getRemonContext()!=null && remon.getRemonContext().getChannel()!=null && remon.getRemonContext().getChannel().getId()!=null)
                    stat += "\nchId:"+remon.getRemonContext().getChannel().getId();
                ((EditText)findViewById(R.id.statBox)).setText(stat);
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

    private void permissionRequest(String permission) {
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(MainActivity.this,permission);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    permission)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {permission}, 1);
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {permission}, 1);
            return;
        }
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
}
