package com.remon.remondroidtest;

import com.remon.remondroid.Remon;
import com.remon.remondroid.RemonObserver;
import com.remon.remondroid.RemonState;
import com.remon.remondroid.data.Channel;
import com.remon.remondroid.util.Logger;

import org.webrtc.MediaStream;

/**
 * Created by calmglow on 16. 10. 20.
 */

public class AppObserver extends RemonObserver {
    private final MainActivity main;
    private Remon remon;

    public AppObserver(MainActivity main, Remon remon){
        super();
        this.main = main;
        this.remon = remon;
    }

    @Override
    public void onCreateChannel(Channel channel){
        String id = channel.getId();
        main.setChannelIdText(id);
    }

    @Override
    public void onAddRemoteStream(MediaStream mediaStream) {
        super.onAddRemoteStream(mediaStream);
        //main.updateVideoViewForConnect();
    }

    @Override
    public void onAddLocalStream(MediaStream mediaStream) {
        super.onAddLocalStream(mediaStream);
        //main.updateVideoViewForInit();
    }

    @Override
    public void onStateChange(RemonState state) {
        super.onStateChange(state);
        main.setStatus(state);
        Logger.i("AppObserver","State:"+state.getState());
//        if (state==RemonState.INIT){
//            try {
//               // main.getRemon().connectChannel("demo");
//
//                //main.updateVideoViewForInit();
//            } catch (RemonException e) {
//                e.printStackTrace();
//            }
//        }
//        main.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                main.updateVideoViewForInit();
//            }
//        });
    }
}
