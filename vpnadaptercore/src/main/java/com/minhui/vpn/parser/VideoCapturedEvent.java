package com.minhui.vpn.parser;

import androidx.annotation.Keep;

@Keep
public class VideoCapturedEvent {
    public String filePath;

    public VideoCapturedEvent(String filePath) {
        this.filePath = filePath;
    }
}
