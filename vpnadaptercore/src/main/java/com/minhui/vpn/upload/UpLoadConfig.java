package com.minhui.vpn.upload;


import androidx.annotation.Keep;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/7.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
@Keep
public class UpLoadConfig implements Serializable {
    private static final long serialVersionUID = 1;
    public static final String VPN_START_TIME = "VPN_Start_Time";
    public static final String APPLICATION = "App_Name";
    public static final String IP_AND_PORT = "IPAndPort";
    public static final String[] DEFAULT_HEADER = {
            VPN_START_TIME,
            APPLICATION,
            IP_AND_PORT,
    };
    private String url;
    private HashMap<String, String> headers;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public UpLoadConfig() {

    }

    public UpLoadConfig(Builder builder) {
        url = builder.url;
        headers = builder.headers;
    }

    public static final class Builder {
        private String url;
        private HashMap<String, String> headers;

        public Builder() {
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder headers(HashMap<String, String> val) {
            headers = val;
            return this;
        }

        public UpLoadConfig build() {
            return new UpLoadConfig(this);
        }
    }
}
