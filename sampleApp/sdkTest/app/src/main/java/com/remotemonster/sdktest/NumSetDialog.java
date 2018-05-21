package com.remotemonster.sdktest;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucas on 2018. 5. 16..
 */

public class NumSetDialog extends Dialog {
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.etNum)
    EditText etNum;
    @BindView(R.id.btnOk)
    Button btnOk;
    private RemonApplication remonApplication;
    private INumSettingListener onINumSettingListener;

    public NumSetDialog(@NonNull Activity activity, String title, INumSettingListener onINumSettingListener) {
        super(activity);
        this.onINumSettingListener = onINumSettingListener;
        setContentView(R.layout.dialog_num_set);
        ButterKnife.bind(this);
        remonApplication = (RemonApplication) activity.getApplicationContext();

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
