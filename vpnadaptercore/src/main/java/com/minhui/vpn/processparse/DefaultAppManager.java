package com.minhui.vpn.processparse;

import android.content.Context;

import com.minhui.vpn.VpnServiceHelper;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.utils.ACache;
import com.minhui.vpn.utils.VPNConstants;

import java.util.concurrent.ConcurrentHashMap;

public class DefaultAppManager {
    private static final String TAG = "DefaultAppManager";
    private ConcurrentHashMap<String, String> defaultMap = null;

    private static DefaultAppManager sInstance = new DefaultAppManager();

    private DefaultAppManager() {

    }

    public static DefaultAppManager getsInstance() {
        return sInstance;
    }

    public void init(Context context) {
        if (defaultMap != null) {
            return;
        }
        try {
            defaultMap = (ConcurrentHashMap) ACache.get(VpnServiceHelper.getContext()).getAsObject(VPNConstants.HOST_APP_MAP);
        } catch (Exception e) {
            VPNLog.e(TAG, "failed get map data");
        }
        if (defaultMap == null) {
            defaultMap = new ConcurrentHashMap<>(512);
        }
    }

    public void add(String host, String pg) {
        if (defaultMap == null) {
            return;
        }
        if (host == null || pg == null) {
            return;
        }
        defaultMap.put(host, pg);
    }

    public String get(String host) {
        if (defaultMap == null) {
            return null;
        }
        if(host==null){
            return null;
        }
        return defaultMap.get(host);
    }

    public void saveData() {
        ACache.get(VpnServiceHelper.getContext()).put(VPNConstants.HOST_APP_MAP, defaultMap);
    }
}
