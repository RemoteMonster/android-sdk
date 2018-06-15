package com.remotemonster.sdktest.sample;

import lombok.Data;

/**
 * Created by lucas on 2018. 5. 16..
 */

@Data
public class ChannelModel {
    private String channelInfo;

    public ChannelModel(String channelInfo) {
        this.channelInfo = channelInfo;
    }
}
