package com.remotemonster.sdktest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.remotemonster.sdk.Config;
import com.remotemonster.sdk.PercentFrameLayout;
import com.remotemonster.sdk.RemonCall;
import com.remotemonster.sdktest.ConfigDialog;
import com.remotemonster.sdktest.R;
import com.remotemonster.sdktest.RemonApplication;

import org.webrtc.SurfaceViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucas on 2018. 4. 26..
 */

public class CallActivity extends AppCompatActivity {
    @BindView(R.id.btnRemonFactoryClose)
    Button btnRemonFactoryClose;
    @BindView(R.id.tvLog)
    TextView tvLog;
    @BindView(R.id.surfRendererLocal)
    SurfaceViewRenderer surfRendererLocal;
    @BindView(R.id.perFrameLocal)
    PercentFrameLayout perFrameLocal;
    @BindView(R.id.surfRendererRemote)
    SurfaceViewRenderer surfRendererRemote;
    @BindView(R.id.perFrameRemote)
    PercentFrameLayout perFrameRemote;
    @BindView(R.id.scvLog)
    ScrollView scvLog;
    @BindView(R.id.btnStatReport)
    Button btnStatReport;
    @BindView(R.id.rlRemoteView)
    RelativeLayout rlRemoteView;


    private RemonCall remonCall;
    private Config mConfig;
    private String connectChId;
    private RemonApplication remonApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        remonApplication = (RemonApplication) getApplicationContext();


        Intent intent = getIntent();
        if (intent.getBooleanExtra("isCreate", false)) {
            ConfigDialog configDialog = new ConfigDialog(CallActivity.this, true, chid -> {
                Config config;
                config = remonApplication.getConfig();
                config.setLocalView(surfRendererLocal);
                config.setRemoteView(surfRendererRemote);

                connectChId = chid;

                remonCall = new RemonCall();
                remonCall.setContext(CallActivity.this);
                setCallback();
                remonCall.connectChannel(connectChId, config);
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

                    remonCall = new RemonCall();
                    remonCall.setContext(CallActivity.this);
                    setCallback();
                    remonCall.connectChannel(connectChId, config);

                });
                configDialog.show();
            } else {
                remonCall = RemonCall.builder()
                        .context(CallActivity.this)
                        .localView(surfRendererLocal)
                        .remoteView(surfRendererRemote)
                        .serviceId(remonApplication.getConfig().getServiceId())
                        .key(remonApplication.getConfig().getKey())
                        .build();
                setCallback();
                remonCall.connectChannel(connectChId);
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
        remonCall.onConnect(() -> addLog("onConnect"));
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
