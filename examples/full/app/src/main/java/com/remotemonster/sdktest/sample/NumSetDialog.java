package com.remotemonster.sdktest.sample;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.remon.sdktest.R;

/**
 * Created by lucas on 2018. 5. 16..
 */

public class NumSetDialog extends Dialog {
    TextView tvTitle;
    EditText etNum;
    Button btnOk;
    private INumSettingListener onINumSettingListener;

    public NumSetDialog(@NonNull Activity activity, String title, INumSettingListener onINumSettingListener) {
        super(activity);
        this.onINumSettingListener = onINumSettingListener;
        setContentView(R.layout.dialog_num_set);


        tvTitle = (TextView) findViewById(R.id.tvTitle);
        etNum = (EditText) findViewById(R.id.etNum);
        btnOk = (Button) findViewById(R.id.btnOk);

        tvTitle.setText(title);

        btnOk.setOnClickListener(v -> {
            onINumSettingListener.NumSetResult(Integer.parseInt(etNum.getText().toString()));
            dismiss();
        });
    }

    public interface INumSettingListener {
        public void NumSetResult(int num);
    }

}
