package com.minhui.networkcapture;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.minhui.networkcapture.ui.PackageShowInfo;
import com.minhui.networkcapture.utils.AppConstants;
import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.VpnServiceHelper;
import com.minhui.vpn.upload.UpLoadConfig;
import com.minhui.vpn.utils.ACache;
import com.minhui.vpn.utils.ThreadProxy;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/8/12.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class SSLVPNUtils {
    public static void startVPN(Context context) {
        Handler handler = new Handler(Looper.getMainLooper());
        SharedPreferences sp = context.getSharedPreferences(AppConstants.DATA_SAVE, MODE_PRIVATE);
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                PackageShowInfo showInfo = (PackageShowInfo) ACache.get(context).getAsObject(AppConstants.SELECT_PACKAGE);
                String selectPackage;
                if (showInfo == null) {
                    selectPackage = null;
                } else {
                    selectPackage = showInfo.packageName;
                }
                ProxyConfig.Instance.setSelectPG(selectPackage);
                ArrayList<String> selectIps = (ArrayList<String>) ACache.get(context).getAsObject(AppConstants.SELECT_IP);
                ArrayList<String> selectHosts = (ArrayList<String>) ACache.get(context).getAsObject(AppConstants.SELECT_HOST);
                ProxyConfig.Instance.setSelectIps(selectIps);
                ProxyConfig.Instance.setSelectHosts(selectHosts);
                UpLoadConfig upLoadConfig = (UpLoadConfig) ACache.get(context).getAsObject(AppConstants.UPLOAD_CONFIG);
                ProxyConfig.Instance.setUpLoadConfig(upLoadConfig);
                boolean autoUpload = sp.getBoolean(AppConstants.AUTO_UPLOAD, false);
                ProxyConfig.Instance.setAutoUpload(autoUpload);
                boolean autoSendToClient = sp.getBoolean(AppConstants.AUTO_SEND_TO_CLIENT, false);
                String clientIP = sp.getString(AppConstants.DESKTOP_CLIENT_IP, null);
                ProxyConfig.Instance.setSendToDesktop(autoSendToClient);
                ProxyConfig.Instance.setClientIP(clientIP);
                boolean autoMatch = sp.getBoolean(AppConstants.AUTO_MATCH_HOST, true);
                ProxyConfig.Instance.setAutoMatchHost(autoMatch);
                boolean isSaveUDP = sp.getBoolean(AppConstants.IS_SAVE_UDP, false);
                ProxyConfig.Instance.setSaveUdp(isSaveUDP);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        VpnServiceHelper.changeVpnRunningStatus(context, true,context. getResources().getString(R.string.app_name));
                    }
                });
            }
        });
        sp.edit().putBoolean(AppConstants.HAS_FULL_USE_APP, true).apply();
    }
}
