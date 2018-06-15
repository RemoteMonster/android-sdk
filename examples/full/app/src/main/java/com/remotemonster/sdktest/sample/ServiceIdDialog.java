package com.remotemonster.sdktest.sample;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import com.remon.sdktest.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucas on 2018. 5. 16..
 */

public class ServiceIdDialog extends Dialog {
    @BindView(R.id.etServiceId)
    EditText etServiceId;
    @BindView(R.id.etKey)
    EditText etKey;
    @BindView(R.id.btnOk)
    Button btnOk;

    private RemonApplication remonApplication;

    public ServiceIdDialog(@NonNull Activity activity) {
        super(activity);
        setContentView(R.layout.dialog_set_seviceid);
        ButterKnife.bind(this);
        remonApplication = (RemonApplication) activity.getApplicationContext();

        etKey.setText(remonApplication.getConfig().getKey());
        etServiceId.setText(remonApplication.getConfig().getServiceId());

        btnOk.setOnClickListener(v -> {
            remonApplication.getConfig().setServiceId(etServiceId.getText().toString());
            remonApplication.getConfig().setKey(etKey.getText().toString());
            dismiss();
        });
    }
}
