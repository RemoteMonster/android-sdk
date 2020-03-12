package com.remotemonster.sdktest.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.remon.sdktest.R;
import com.remon.sdktest.databinding.ActivityCastBinding;
import com.remotemonster.sdk.Config;
import com.remotemonster.sdk.RemonCast;

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
                remonCast = new RemonCast();

                Config config;
                config = remonApplication.getConfig();
                config.setLocalView(mBinding.surfRendererLocal);
                config.setContext(CastActivity.this);
                setCallback(false, remonCast);

                connectChId = chid;
                remonCast.create(connectChId, config);
            });
            configDialog.show();
        } else {
            /* List에 있던 방송 시청 */
            connectChId = intent.getStringExtra("chid");


            if (intent.getBooleanExtra("setConfig", false)) {
                /* Config 수정 후 시청 */
                ConfigDialog configDialog = new ConfigDialog(CastActivity.this, false, chid -> {
                    castViewer = new RemonCast();
                    Config config;
                    config = remonApplication.getConfig();
                    config.setRemoteView(mBinding.surfRendererRemote);
                    config.setContext(CastActivity.this);
                    setCallback(true, castViewer);

                    castViewer.join(connectChId, config);
                });
                configDialog.show();
            } else {
                /* 즉시 시청 */
                castViewer = RemonCast.builder()
                        .context(CastActivity.this)
                        .remoteView(mBinding.surfRendererRemote)
                        .serviceId(remonApplication.getConfig().getServiceId())
                        .key(remonApplication.getConfig().getKey())
                        .build();
                setCallback(true, castViewer);

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
                        .build();
                setCallback(true, castViewer);

                castViewer.join(remonCast.getId());
            }
        });


        mBinding.btnSpeakerOnOff.setOnClickListener(v -> {
            if (castViewer != null) {
                isSpeakerOn = isSpeakerOn ? (isSpeakerOn = false) : (isSpeakerOn = true);
                castViewer.setSpeakerphoneOn(isSpeakerOn);
            }
        });


        // 영상크기 변환시 사용
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

    private void setCallback(boolean isCastView, RemonCast remonCast) {
        remonCast.onInit(() -> addLog("onInit"));
        remonCast.onCreate((String id) -> addLog("onCreate : " + id));
        remonCast.onComplete(() -> {
            addLog("onComplete");
            this.isCastView = isCastView;
        });
        remonCast.onJoin(() -> {
            addLog("onJoin");
            mBinding.surfRendererRemote.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        });
        remonCast.onClose(this::finish);
        remonCast.onError(e -> addLog("error code : " + e.getErrorCode()));
        remonCast.onStat(report -> {
            if (isCastView) {
                //addLog("Receive report - fps : " + report.getRemoteFrameRate());
            } else {
                //addLog("Receive report - fps : " + report.getLocalFrameRate());
            }
        });
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
        if (remonCast != null) {
            remonCast.close();
        }
        if (castViewer != null) {
            castViewer.close();
        }
        super.onDestroy();
    }
}

