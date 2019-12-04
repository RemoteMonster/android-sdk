package com.remotemonster.sdktest.sample;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.remon.sdktest.R;
import com.remon.sdktest.databinding.ActivityCallBinding;
import com.remotemonster.sdk.Config;
import com.remotemonster.sdk.RemonCall;

import org.webrtc.RendererCommon;

/**
 * Created by lucas on 2018. 4. 26..
 */

public class CallActivity extends AppCompatActivity {
    private RemonCall remonCall;
    private String connectChId;
    private RemonApplication remonApplication;
    private boolean isSpeakerOn = true;
    private ActivityCallBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 화면 계속 켜져있도록
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_call);
        remonApplication = (RemonApplication) getApplicationContext();

        Intent intent = getIntent();
        if (intent.getBooleanExtra("isCreate", false)) {
            ConfigDialog configDialog = new ConfigDialog(CallActivity.this, true, chid -> {
                remonCall = new RemonCall();
                Config config;
                config = remonApplication.getConfig();
                config.setLocalView(mBinding.surfRendererLocal);
                config.setRemoteView(mBinding.surfRendererRemote);
                config.setActivity(CallActivity.this);
                setCallback();
                connectChId = chid;
                remonCall.connect(connectChId, config);
            });
            configDialog.show();
        } else {
            connectChId = intent.getStringExtra("chid");
            if (intent.getBooleanExtra("setConfig", false)) {
                ConfigDialog configDialog = new ConfigDialog(CallActivity.this, false, chid -> {
                    Config config;
                    config = remonApplication.getConfig();
                    config.setLocalView(mBinding.surfRendererLocal);
                    config.setRemoteView(mBinding.surfRendererRemote);
                    config.setRestHost(remonApplication.getConfig().restHost);
                    config.setSocketUrl(remonApplication.getConfig().socketUrl);
                    config.setActivity(CallActivity.this);
                    remonCall = new RemonCall();
                    setCallback();
                    remonCall.connect(connectChId, config);

                });
                configDialog.show();
            } else {
                remonCall = RemonCall.builder()
                        .context(CallActivity.this)
                        .localView(mBinding.surfRendererLocal)
                        .remoteView(mBinding.surfRendererRemote)
                        .serviceId(remonApplication.getConfig().getServiceId())
                        .key(remonApplication.getConfig().getKey())
                        .restUrl(remonApplication.getConfig().restHost)
                        .wssUrl(remonApplication.getConfig().socketUrl)
                        .build();
                setCallback();
                remonCall.connect(connectChId);
            }
        }

        mBinding.btnSpeakerOnOff.setOnClickListener(view -> {
            if (remonCall != null) {
                isSpeakerOn = isSpeakerOn ? (isSpeakerOn = false) : (isSpeakerOn = true);
                remonCall.setSpeakerphoneOn(isSpeakerOn);
            }
        });

        mBinding.btnSendMsg.setOnClickListener(view -> {
            if (remonCall != null) {
                remonCall.sendMessage("send Message Test");
            }
        });


        mBinding.btnRemonFactoryClose.setOnClickListener(v -> {
            addLog("Start close");
            remonCall.close();
        });

        mBinding.btnStatReport.setOnClickListener(v -> remonCall.enableStatView(true, mBinding.rlRemoteView));
    }

    private void setCallback() {
        remonCall.onInit(() -> addLog("onInit"));
        remonCall.onMessage(msg -> {
            addLog(msg);
            Toast.makeText(CallActivity.this, "receive : " + msg, Toast.LENGTH_SHORT).show();
        });
        remonCall.onConnect((String id) -> addLog("onConnect : " + id));
        remonCall.onComplete(() ->  {
            mBinding.surfRendererLocal.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
            mBinding.surfRendererRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        });
        remonCall.onClose((closeType) -> addLog("onClose : "+ closeType.toString()));
        remonCall.onError(e -> addLog("error code : " + e.getRemonCode().toString() + " / " + e.getDescription()));
        remonCall.onStat(report -> addLog("Receive report - fps : "));//+report.getRemoteFrameRate()));
    }

    private String mPriorLog = "";

    private void addLog(String log) {
        mPriorLog = mPriorLog + log + "\n";

        mBinding.tvLog.setText(mPriorLog);
        mBinding.scvLog.scrollTo(0, mBinding.scvLog.getBottom());

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
