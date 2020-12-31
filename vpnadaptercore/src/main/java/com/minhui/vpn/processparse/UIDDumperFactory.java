package com.minhui.vpn.processparse;

import android.os.Build;

import com.minhui.vpn.ProxyConfig;

public class UIDDumperFactory {
    public static UIDDumper createUIDDumper(ProxyConfig proxyConfig) {
        if (!proxyConfig.verifyUID()) {
            return new EmptyUIDDumper();
        }
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.Q) {
            return new ConnectManagerUIDDumper();
        }
        return  new NetFileManagerDumper();
    }
}
