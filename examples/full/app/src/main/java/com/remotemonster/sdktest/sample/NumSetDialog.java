package com.remotemonster.sdktest.sample;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.remon.sdktest.R;

/**
 * Created by lucas on 2018. 5. 16..
 */

public class NumSetDialog extends Dialog {
    TextView tvTitle;
    EditText etNum;
    Button btnOk;

    private RemonApplication remonApplication;
    private INumSettingListener onINumSettingListener;

    public NumSetDialog(@NonNull Activity activity, String title, INumSettingListener onINumSettingListener) {
        super(activity);
        this.onINumSettingListener = onINumSettingListener;
        setContentView(R.layout.dialog_num_set);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        etNum = (EditText) findViewById(R.id.etNum);
        btnOk = (Button) findViewById(R.id.btnOk);

        remonApplication = (RemonApplication) activity.getApplicationContext();
        tvTitle.setText(title);

        btnOk.setOnClickListener(v -> {
            if (etNum.getText().equals("")) {
                dismiss();
            } else if (isNumeric(etNum.getText().toString())) {
                onINumSettingListener.NumSetResult(Integer.parseInt(etNum.getText().toString()));
                dismiss();
            } else {
                dismiss();
            }
        });
    }

    public interface INumSettingListener {
        public void NumSetResult(int num);
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}
