package com.remotemonster.sdktest.sample;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.remotemonster.sdk.Config;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by lucas on 2018. 5. 16..
 */

@Getter
@Setter
public class RemonApplication extends Application {
    private Config config;
    public static final String REST_HOST = "https://matiz.remotemonster.com/rest/";
    public static final String WSS_HOST = "wss://matiz.remotemonster.com/ws";
//    public static final String WS_HOST = "ws://signal.remotemonster.com:8081/ws";

    @Override
    public void onCreate() {
        super.onCreate();

        initConfig();
    }

    private void initConfig() {
        config = new Config();
        config.setVideoCodec("H264");
        config.setRestHost(REST_HOST);
        config.setSocketUrl(WSS_HOST);
        config.setVideoWidth(640);
        config.setVideoHeight(480);
        config.setVideoFps(30);
        config.setStartVideoBitrate(1000);
        config.setAudioStartBitrate(32);
        config.setStatInterval(2000);
        config.setVideoCall(true);
        config.setLogLevel(Log.INFO);
        config.setKey("1234567890");
        config.setServiceId("SERVICEID1");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(getBaseContext());
    }
}
