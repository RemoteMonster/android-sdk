package com.remotemonster.sdktest.sample;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.remon.sdktest.R;
import com.remon.sdktest.databinding.DialogSetConfigBinding;
import com.remotemonster.sdk.data.AudioType;

/**
 * Created by lucas on 2018. 5. 11..
 */

public class ConfigDialog extends Dialog {
    LinearLayout llVideoWidth;
    LinearLayout llVideoHeight;
    LinearLayout llVideoFps;
    LinearLayout llfirstVideoBitrate;
    Button btnOk;
    EditText etChannelName;
    LinearLayout llChannelName;
    Spinner spVideoCodec;
    TextView tvVideoWidth;
    TextView tvVideoHeight;
    TextView tvVideoFps;
    TextView tvFirstVideoBitrate;
    CheckBox cbEnableVideoCall;
    Spinner spAudioType;
    Spinner spAudioCodec;

    private Activity activity;
    private RemonApplication remonApplication;
    private IConfigSettingListener onINameInputDlgListener;

    public ConfigDialog(@NonNull Activity activity, boolean isCreate, IConfigSettingListener iConfigSettingListener) {
        super(activity, R.style.FullScreenDialogStyle);
        this.onINameInputDlgListener = iConfigSettingListener;
        this.activity = activity;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_set_config);

        llVideoWidth = (LinearLayout) findViewById(R.id.llVideoWidth);
        llVideoHeight = (LinearLayout) findViewById(R.id.llVideoHeight);
        llVideoFps = (LinearLayout) findViewById(R.id.llVideoFps);
        llfirstVideoBitrate = (LinearLayout) findViewById(R.id.llfirstVideoBitrate);
        btnOk = (Button) findViewById(R.id.btnOk);
        etChannelName = (EditText) findViewById(R.id.etChannelName);
        llChannelName = (LinearLayout) findViewById(R.id.llChannelName);
        spVideoCodec = (Spinner) findViewById(R.id.spVideoCodec);
        tvVideoWidth = (TextView) findViewById(R.id.tvVideoWidth);
        tvVideoHeight = (TextView) findViewById(R.id.tvVideoHeight);
        tvVideoFps = (TextView) findViewById(R.id.tvVideoFps);
        tvFirstVideoBitrate = (TextView) findViewById(R.id.tvFirstVideoBitrate);
        cbEnableVideoCall = (CheckBox) findViewById(R.id.cbEnableVideoCall);
        spAudioType = (Spinner) findViewById(R.id.spAudioType);
        spAudioCodec = (Spinner) findViewById(R.id.spAudioCodec);

        setCanceledOnTouchOutside(false);
        remonApplication = (RemonApplication) activity.getApplicationContext();

        if (isCreate) {
            llChannelName.setVisibility(View.VISIBLE);
        } else {
            llChannelName.setVisibility(View.GONE);
        }

        btnOk.setOnClickListener(v -> {
            if (isCreate) {
                if (!etChannelName.getText().toString().equals("")) {
                    onINameInputDlgListener.configSetResult(etChannelName.getText().toString());
                    dismiss();
                } else {
                    Toast.makeText(activity, "Please enter your channel name.", Toast.LENGTH_SHORT).show();
                }
            } else {
                onINameInputDlgListener.configSetResult(etChannelName.getText().toString());
                dismiss();
            }
        });


        spVideoCodec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        remonApplication.getConfig().setVideoCodec("VP8");
                        break;

                    case 1:
                        remonApplication.getConfig().setVideoCodec("VP9");
                        break;

                    case 2:
                        remonApplication.getConfig().setVideoCodec("H264");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spVideoCodec.setSelection(2);

        spAudioCodec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        remonApplication.getConfig().setAudioCodec("OPUS");
                        break;

                    case 1:
                        remonApplication.getConfig().setVideoCodec("ISAC");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spAudioCodec.setSelection(0);

        llVideoWidth.setOnClickListener(v -> {
            NumSetDialog numSetDialog = new NumSetDialog(activity, "Video Width?", num -> {
                remonApplication.getConfig().setVideoWidth(num);
                tvVideoWidth.setText(num + "");
                closeSoftKey();
            });
            numSetDialog.show();
        });

        llVideoHeight.setOnClickListener(v -> {
            NumSetDialog numSetDialog = new NumSetDialog(activity, "Video Height?", num -> {
                remonApplication.getConfig().setVideoHeight(num);
                tvVideoHeight.setText(num + "");
                closeSoftKey();
            });
            numSetDialog.show();
        });


        llVideoFps.setOnClickListener(v -> {
            NumSetDialog numSetDialog = new NumSetDialog(activity, "Video Fps?", num -> {
                remonApplication.getConfig().setVideoFps(num);
                tvVideoFps.setText(num + "");
                closeSoftKey();
            });
            numSetDialog.show();

        });

        llfirstVideoBitrate.setOnClickListener(v -> {
            NumSetDialog numSetDialog = new NumSetDialog(activity, "First Video Bitrate?", num -> {
                remonApplication.getConfig().setStartVideoBitrate(num);
                tvFirstVideoBitrate.setText(num + "");
                closeSoftKey();
            });
            numSetDialog.show();
        });

        cbEnableVideoCall.setOnClickListener(v -> {
            if (cbEnableVideoCall.isChecked()) {
                remonApplication.getConfig().setVideoCall(true);
            } else {
                remonApplication.getConfig().setVideoCall(false);
            }
        });

        spAudioType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
                    case 0:
                        remonApplication.getConfig().setAudioType(AudioType.VOICE);
                        break;

                    case 1:
                        remonApplication.getConfig().setAudioType(AudioType.MUSIC);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (activity instanceof CallActivity) {
            spAudioType.setSelection(0);
        } else {
            spAudioType.setSelection(1);
        }


    }

    private void closeSoftKey() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public interface IConfigSettingListener {
        public void configSetResult(String chid);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        activity.finish();
    }
}
