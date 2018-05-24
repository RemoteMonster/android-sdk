package com.remotemonster.sdktest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.remotemonster.sdk.Config;
import com.remotemonster.sdk.PercentFrameLayout;
import com.remotemonster.sdk.RemonCast;
import com.remotemonster.sdktest.ConfigDialog;
import com.remotemonster.sdktest.R;
import com.remotemonster.sdktest.RemonApplication;

import org.webrtc.SurfaceViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CastActivity extends AppCompatActivity {
    @BindView(R.id.btnRemonCastClose)
    Button btnRemonCastClose;
    @BindView(R.id.tvLog)
    TextView tvLog;
    @BindView(R.id.scvLog)
    ScrollView scvLog;
    @BindView(R.id.surfRendererLocal)
    SurfaceViewRenderer surfRendererLocal;
    @BindView(R.id.perFrameLocal)
    PercentFrameLayout perFrameLocal;
    @BindView(R.id.surfRendererRemote)
    SurfaceViewRenderer surfRendererRemote;
    @BindView(R.id.perFrameRemote)
    PercentFrameLayout perFrameRemote;
    @BindView(R.id.btnStatReport)
    Button btnStatReport;
    @BindView(R.id.btnViewCast)
    Button btnViewCast;
    @BindView(R.id.rlRemoteView)
    RelativeLayout rlRemoteView;



    private RemonApplication remonApplication;
    private String connectChId;
    private RemonCast remonCast = null;
    private RemonCast remonViewer = null;
    private boolean isCastView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cast);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        remonApplication = (RemonApplication) getApplicationContext();


        Intent intent = getIntent();
        if (intent.getBooleanExtra("isCreate", false)) {
            /* config set 후 방송생성*/
            ConfigDialog configDialog = new ConfigDialog(CastActivity.this, true, chid -> {
                Config config;
                config = remonApplication.getConfig();
                config.setLocalView(surfRendererLocal);

                connectChId = chid;

                remonCast = new RemonCast();
                remonCast.setContext(CastActivity.this);

                setCallback(false);
                remonCast.createRoom(connectChId, config);
            });
            configDialog.show();
        } else {
            /* List에 있던 방송 시청 */
            connectChId = intent.getStringExtra("chid");
            if (intent.getBooleanExtra("setConfig", false)) {
                ConfigDialog configDialog = new ConfigDialog(CastActivity.this, false, chid -> {
                    Config config;
                    config = remonApplication.getConfig();
                    config.setRemoteView(surfRendererRemote);

                    remonCast = new RemonCast();
                    setCallback(false);
                    remonCast.joinRoom(connectChId, config);
                });
                configDialog.show();
            } else {
                remonViewer = RemonCast.builder()
                        .context(CastActivity.this)
                        .remoteView(surfRendererRemote)
                        .serviceId(remonApplication.getConfig().getServiceId())
                        .key(remonApplication.getConfig().getKey())
                        .build();
                setCallback(true);
                remonViewer.joinRoom(connectChId);
            }
        }


        btnRemonCastClose.setOnClickListener(v -> {
            addLog("Start close");
            if (remonCast != null) {
                remonCast.close();
            }
            if (remonViewer != null) {
                remonViewer.close();
            }

        });


        btnStatReport.setOnClickListener(v -> remonCast.enableStatView(true, rlRemoteView));

        btnViewCast.setOnClickListener(v -> {
            if (!isCastView) {
                /* 자신이 시청중이 아니라면 방송시청*/
                remonViewer = RemonCast.builder()
                        .context(CastActivity.this)
                        .remoteView(surfRendererRemote)
                        .serviceId(remonApplication.getConfig().getServiceId())
                        .key(remonApplication.getConfig().getKey())
                        .build();
                setCallback(true);
                remonViewer.joinRoom(remonCast.getId());
            }
        });
    }

    private void setCallback(boolean isCastView) {
        if (isCastView) {
            remonViewer.onInit(() -> addLog("onInit"));
            remonViewer.onConnect(() -> addLog("onConnect"));
            remonViewer.onComplete(() -> {
                addLog("onComplete");
                this.isCastView = isCastView;
            });
            remonViewer.onClose(() -> addLog("onClose"));
            remonViewer.onError(e -> addLog("error code : " + e.getRemonCode().toString()));
            remonViewer.onStat(report -> addLog("Print report"));
        } else {
            remonCast.onInit(() -> addLog("onInit"));
            remonCast.onConnect(() -> addLog("onConnect"));
            remonCast.onComplete(() -> addLog("onComplete"));
            remonCast.onClose(() -> addLog("onClose"));
            remonCast.onError(e -> addLog("error code : " + e.getRemonCode().toString()));
            remonCast.onStat(report -> addLog(report.getFullStatReport()));
        }
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
        if (remonCast != null) {
            remonCast.close();
        }
        if (remonViewer != null) {
            remonViewer.close();
        }
        super.onDestroy();
    }
}
