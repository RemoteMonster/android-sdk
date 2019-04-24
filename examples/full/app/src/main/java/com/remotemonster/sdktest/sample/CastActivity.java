package com.remotemonster.sdktest.sample;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.remon.sdktest.R;
import com.remon.sdktest.databinding.ActivityCastBinding;
import com.remotemonster.sdk.Config;
import com.remotemonster.sdk.RemonCast;
import com.remotemonster.sdk.data.AudioType;

import org.webrtc.RendererCommon;

public class CastActivity extends AppCompatActivity {
    private RemonApplication remonApplication;
    private String connectChId;
    private RemonCast remonCast = null;
    private RemonCast castViewer = null;
    private boolean isCastView = false;
    private boolean isSpeakerOn = true;
    private boolean isRemoteFullScreen = false;
    private ActivityCastBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_cast);

        remonApplication = (RemonApplication) getApplicationContext();


        isRemoteFullScreen = false;
        mBinding.perFrameLocal.setPosition(0, 50, 50, 50);
        mBinding.perFrameRemote.setPosition(50, 50, 50, 50);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("isCreate", false)) {
            /* config set 후 방송생성*/
            ConfigDialog configDialog = new ConfigDialog(CastActivity.this, true, chid -> {
                Config config;
                config = remonApplication.getConfig();
                config.setLocalView(mBinding.surfRendererLocal);
                config.setActivity(CastActivity.this);
                config.logLevel = Log.VERBOSE;
                config.setAudioStartBitrate(256);
                config.setAudioType(AudioType.MUSIC);
                connectChId = chid;
                remonCast = new RemonCast();
                setCallback(false);
                remonCast.create(connectChId, config);
            });
            configDialog.show();
        } else {
            /* List에 있던 방송 시청 */
            connectChId = intent.getStringExtra("chid");
            if (intent.getBooleanExtra("setConfig", false)) {
                ConfigDialog configDialog = new ConfigDialog(CastActivity.this, false, chid -> {
                    Config config;
                    config = remonApplication.getConfig();
                    config.setRemoteView(mBinding.surfRendererRemote);
                    config.setActivity(CastActivity.this);
                    config.setVideoCodec("VP8");
                    remonCast = new RemonCast();
                    setCallback(true);
                    remonCast.join(connectChId, config);
                });
                configDialog.show();
            } else {
                castViewer = RemonCast.builder()
                        .context(CastActivity.this)
                        .remoteView(mBinding.surfRendererRemote)
                        .serviceId(remonApplication.getConfig().getServiceId())
                        .key(remonApplication.getConfig().getKey())
                        .restUrl(remonApplication.getConfig().restHost)
                        .wssUrl(remonApplication.getConfig().socketUrl)
                        .videoCodec("VP8")
                        .build();
                setCallback(true);
                castViewer.join(connectChId);
            }
        }


        mBinding.btnRemonCastClose.setOnClickListener(v -> {
            addLog("Start close");
            if (remonCast != null) {
                remonCast.close();
            }
            if (castViewer != null) {
                castViewer.close();
            }
        });


        mBinding.btnStatReport.setOnClickListener(v -> remonCast.enableStatView(true, mBinding.rlRemoteView));
        mBinding.btnStatReport.setOnClickListener(view -> {
            if (isCastView) {
                castViewer.enableStatView(true, mBinding.rlRemoteView);
            } else {
                remonCast.enableStatView(true, mBinding.rlRemoteView);
            }
        });

        mBinding.btnViewCast.setOnClickListener(v -> {
            if (!isCastView) {
                /* 자신이 시청중이 아니라면 방송시청*/
                castViewer = RemonCast.builder()
                        .context(CastActivity.this)
                        .remoteView(mBinding.surfRendererRemote)
                        .serviceId(remonApplication.getConfig().getServiceId())
                        .key(remonApplication.getConfig().getKey())
                        .restUrl(remonApplication.getConfig().restHost)
                        .wssUrl(remonApplication.getConfig().socketUrl)
                        .build();
                setCallback(true);
                castViewer.join(remonCast.getId());
            }
        });

        mBinding.btnSpeakerOnOff.setOnClickListener(v -> {
            if (castViewer != null) {
                isSpeakerOn = isSpeakerOn ? (isSpeakerOn = false) : (isSpeakerOn = true);
                castViewer.setSpeakerphoneOn(isSpeakerOn);
            }
        });

        mBinding.imvRemoteScreenChange.setOnClickListener(view -> runOnUiThread(() -> {
            if (isRemoteFullScreen) {
                isRemoteFullScreen = false;
                mBinding.perFrameRemote.setPosition(50, 50, 50, 50);
                mBinding.perFrameRemote.requestLayout();
            } else {
                isRemoteFullScreen = true;
                mBinding.perFrameRemote.setPosition(0, 0, 100, 100);
                mBinding.perFrameRemote.requestLayout();
            }
        }));
    }

    private void setCallback(boolean isCastView) {

        if (isCastView) {
            castViewer.onInit(() -> addLog("onInit"));
            castViewer.onComplete(() -> {
                addLog("onComplete");
                this.isCastView = isCastView;
            });
            castViewer.onJoin(() -> {
                addLog("onJoin");
                runOnUiThread(() -> mBinding.surfRendererRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL));
            });
            castViewer.onClose(() -> finish());
            castViewer.onError(e -> addLog("error code : " + e.getRemonCode().toString()));
            castViewer.onStat(report -> addLog("Print report"));
        } else {
            remonCast.onInit(() -> addLog("onInit"));
            remonCast.onCreate((String id) -> addLog("onCreate : " + id));
            remonCast.onComplete(() -> addLog("onComplete"));
            remonCast.onClose(() -> finish());
            remonCast.onError(e -> addLog("error code : " + e.getRemonCode().toString()));
            remonCast.onStat(report -> addLog("Print report"));
        }
    }


    private String mPriorLog = "";

    private void addLog(String log) {
        mPriorLog = mPriorLog + log + "\n";
        runOnUiThread(() -> {
            mBinding.tvLog.setText(mPriorLog);
            mBinding.scvLog.scrollTo(0, mBinding.scvLog.getBottom());
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    @Override
    protected void onDestroy() {
        if (remonCast != null) {
            remonCast.close();
        }
        if (castViewer != null) {
            castViewer.close();
        }
        super.onDestroy();
    }
}

