package com.minhui.vpn.oknet;

import okhttp3.OkHttpClient;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/6/10.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class OKHttpManager {

    private final OkHttpClient okHttpClient;

    private OKHttpManager() {
        okHttpClient = new OkHttpClient.Builder()
                .socketFactory(new VPNSocketFactory())
                .build();

    }

    private static class InnerClass {
        static OKHttpManager instance = new OKHttpManager();
    }
    public static OkHttpClient getOkHttpClient(){
        return InnerClass.instance.okHttpClient;
    }
}
