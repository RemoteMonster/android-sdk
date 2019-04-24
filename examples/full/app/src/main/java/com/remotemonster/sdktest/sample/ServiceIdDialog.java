package com.remotemonster.sdktest.sample;

import android.app.Activity;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import com.remon.sdktest.R;
import com.remon.sdktest.databinding.DialogSetSeviceidBinding;

/**
 * Created by lucas on 2018. 5. 16..
 */

public class ServiceIdDialog extends Dialog {
    EditText etServiceId;
    EditText etKey;
    Button btnOk;

    private RemonApplication remonApplication;

    public ServiceIdDialog(@NonNull Activity activity) {
        super(activity);
        setContentView(R.layout.dialog_set_seviceid);
        remonApplication = (RemonApplication) activity.getApplicationContext();

        etServiceId = (EditText) findViewById(R.id.etServiceId);
        etKey = (EditText) findViewById(R.id.etKey);
        btnOk = (Button) findViewById(R.id.btnOk);

        etKey.setText(remonApplication.getConfig().getKey());
        etServiceId.setText(remonApplication.getConfig().getServiceId());

        btnOk.setOnClickListener(v -> {
            remonApplication.getConfig().setServiceId(etServiceId.getText().toString());
            remonApplication.getConfig().setKey(etKey.getText().toString());
            dismiss();
        });
    }
}
