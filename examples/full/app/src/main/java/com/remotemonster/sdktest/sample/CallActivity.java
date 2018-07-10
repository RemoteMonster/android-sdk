package com.remotemonster.sdktest.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.remon.sdktest.R;
import com.remotemonster.sdk.Config;
import com.remotemonster.sdk.PercentFrameLayout;
import com.remotemonster.sdk.RemonCall;
import com.remotemonster.sdk.core.SurfaceViewRenderer;

/**
 * Created by lucas on 2018. 4. 26..
 */

public class CallActivity extends AppCompatActivity {
    Button btnRemonFactoryClose;
    TextView tvLog;
    SurfaceViewRenderer surfRendererLocal;
    PercentFrameLayout perFrameLocal;
    SurfaceViewRenderer surfRendererRemote;
    PercentFrameLayout perFrameRemote;
    ScrollView scvLog;
    Button btnStatReport;
    RelativeLayout rlRemoteView;

    private RemonCall remonCall;
    private Config mConfig;
    private String connectChId;
    private RemonApplication remonApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        remonApplication = (RemonApplication) getApplicationContext();

        btnRemonFactoryClose = (Button) findViewById(R.id.btnRemonFactoryClose);
        tvLog = (TextView) findViewById(R.id.tvLog);
        surfRendererLocal = (SurfaceViewRenderer) findViewById(R.id.surfRendererLocal);
        perFrameLocal = (PercentFrameLayout) findViewById(R.id.perFrameLocal);
        surfRendererRemote = (SurfaceViewRenderer) findViewById(R.id.surfRendererRemote);
        perFrameRemote = (PercentFrameLayout) findViewById(R.id.perFrameRemote);
        scvLog = (ScrollView) findViewById(R.id.scvLog);
        btnStatReport = (Button) findViewById(R.id.btnStatReport);
        rlRemoteView = (RelativeLayout) findViewById(R.id.rlRemoteView);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("isCreate", false)) {
            ConfigDialog configDialog = new ConfigDialog(CallActivity.this, true, chid -> {
                Config config;
                config = remonApplication.getConfig();
                config.setLocalView(surfRendererLocal);
                config.setRemoteView(surfRendererRemote);
                config.setRestHost(remonApplication.getConfig().restHost);
                config.setSocketUrl(remonApplication.getConfig().socketUrl);
                connectChId = chid;

                remonCall = new RemonCall();
                remonCall.setContext(CallActivity.this);
                setCallback();
                remonCall.connect(connectChId, config);
            });
            configDialog.show();
        } else {
            connectChId = intent.getStringExtra("chid");
            if (intent.getBooleanExtra("setConfig", false)) {
                ConfigDialog configDialog = new ConfigDialog(CallActivity.this, false, chid -> {
                    Config config;
                    config = remonApplication.getConfig();
                    config.setLocalView(surfRendererLocal);
                    config.setRemoteView(surfRendererRemote);
                    config.setRestHost(remonApplication.getConfig().restHost);
                    config.setSocketUrl(remonApplication.getConfig().socketUrl);
                    remonCall = new RemonCall();
                    remonCall.setContext(CallActivity.this);
                    setCallback();
                    remonCall.connect(connectChId, config);

                });
                configDialog.show();
            } else {
                remonCall = RemonCall.builder()
                        .context(CallActivity.this)
                        .audioType("music")
                        .localView(surfRendererLocal)
                        .remoteView(surfRendererRemote)
                        .serviceId(remonApplication.getConfig().getServiceId())
                        .key(remonApplication.getConfig().getKey())
                        .restUrl(remonApplication.getConfig().restHost)
                        .wssUrl(remonApplication.getConfig().socketUrl)
                        .build();
                setCallback();
                remonCall.connect(connectChId);
            }
        }

        btnRemonFactoryClose.setOnClickListener(v -> {
            addLog("Start close");
            remonCall.close();
        });

        btnStatReport.setOnClickListener(v -> remonCall.enableStatView(true, rlRemoteView));
    }


    private void setCallback() {
        remonCall.onInit(() -> addLog("onInit"));
        remonCall.onConnect((String id) -> addLog("onConnect : " + id));
        remonCall.onComplete(() -> addLog("onComplete"));
        remonCall.onClose(() -> addLog("onClose"));
        remonCall.onError(e -> addLog("error code : " + e.getRemonCode().toString()));
        remonCall.onStat(report -> addLog("Print report"));
    }

    private String mPriorLog = "";

    private void addLog(String log) {
        mPriorLog = mPriorLog + log + "\n";
        runOnUiThread(() -> {
            tvLog.setText(mPriorLog);
            scvLog.scrollTo(0, scvLog.getBottom());
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onDestroy() {
        if (remonCall != null) {
            remonCall.close();
        }
        super.onDestroy();
    }
}
