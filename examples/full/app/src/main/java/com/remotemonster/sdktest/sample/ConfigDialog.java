package com.remotemonster.sdktest.sample;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.remon.sdktest.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucas on 2018. 5. 11..
 */

public class ConfigDialog extends Dialog {
    @BindView(R.id.llVideoWidth)
    LinearLayout llVideoWidth;
    @BindView(R.id.llVideoHeight)
    LinearLayout llVideoHeight;
    @BindView(R.id.llVideoFps)
    LinearLayout llVideoFps;
    @BindView(R.id.llfirstVideoBitrate)
    LinearLayout llfirstVideoBitrate;
    @BindView(R.id.btnOk)
    Button btnOk;
    @BindView(R.id.etChannelName)
    EditText etChannelName;
    @BindView(R.id.llChannelName)
    LinearLayout llChannelName;
    @BindView(R.id.spNatCodec)
    Spinner spNatCodec;
    @BindView(R.id.tvVideoWidth)
    TextView tvVideoWidth;
    @BindView(R.id.tvVideoHeight)
    TextView tvVideoHeight;
    @BindView(R.id.tvVideoFps)
    TextView tvVideoFps;
    @BindView(R.id.tvFirstVideoBitrate)
    TextView tvFirstVideoBitrate;
    @BindView(R.id.cbEnableVideoCall)
    CheckBox cbEnableVideoCall;
    @BindView(R.id.llEnableVideoCall)
    LinearLayout llEnableVideoCall;

    private Activity activity;
    private RemonApplication remonApplication;
    private IConfigSettingListener onINameInputDlgListener;

    public ConfigDialog(@NonNull Activity activity, boolean isCreate, IConfigSettingListener iConfigSettingListener) {
        super(activity, R.style.FullScreenDialogStyle);
        this.onINameInputDlgListener = iConfigSettingListener;
        this.activity = activity;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_set_config);
        ButterKnife.bind(this);
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


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.videoCodecs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNatCodec.setAdapter(adapter);
        spNatCodec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spNatCodec.setSelection(2);


        llVideoWidth.setOnClickListener(v -> {
            NumSetDialog numSetDialog = new NumSetDialog(activity, "Video Width?", num -> {
                remonApplication.getConfig().setVideoWidth(num);
                tvVideoWidth.setText(num+"");
                closeSoftKey();
            });
            numSetDialog.show();
        });

        llVideoHeight.setOnClickListener(v -> {
            NumSetDialog numSetDialog = new NumSetDialog(activity, "Video Height?", num -> {
                remonApplication.getConfig().setVideoHeight(num);
                tvVideoHeight.setText(num+"");
                closeSoftKey();
            });
            numSetDialog.show();
        });


        llVideoFps.setOnClickListener(v -> {
            NumSetDialog numSetDialog = new NumSetDialog(activity, "Video Fps?", num -> {
                remonApplication.getConfig().setVideoFps(num);
                tvVideoFps.setText(num+"");
                closeSoftKey();
            });
            numSetDialog.show();

        });

        llfirstVideoBitrate.setOnClickListener(v -> {
            NumSetDialog numSetDialog = new NumSetDialog(activity, "First Video Bitrate?", num -> {
                remonApplication.getConfig().setStartVideoBitrate(num);
                tvFirstVideoBitrate.setText(num+"");
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
    }

    private void closeSoftKey() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
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
