package com.minhui.vpn;


import android.content.Context;


import androidx.annotation.Keep;

import com.minhui.vpn.greenDao.DaoSession;
import com.minhui.vpn.greenDao.SessionHelper;
import com.minhui.vpn.service.CaptureVpnService;
import com.minhui.vpn.upload.UpLoadConfig;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Keep
public class ProxyConfig {

    public static final ProxyConfig Instance = new ProxyConfig();
    String mSessionName;
    int mMtu;
    private List<VpnStatusListener> mVpnStatusListeners = new ArrayList<>();
    private ArrayList<String> selectIps;
    private ArrayList<String> selectHosts;
    private String selectPG;
    UpLoadConfig upLoadConfig;
    Boolean autoUpload;
    private boolean autoMatchHost = true;
    private boolean isSaveUdp = false;
    private String pgName;
    private boolean mVerifyUID = true;
    private boolean mSaveData =true;
    private boolean mUseSSL =true;
    private DaoSession mDaoSession;

    public boolean isSaveData(){
        return mSaveData;
    }
    public boolean isUseSSL(){
        return mUseSSL;
    }
    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getClientIP() {
        return clientIP;
    }

    private String clientIP;
    private boolean sendToDesktop;
    private List<String >captureVideo =new ArrayList<>();
    public void setSendToDesktop(boolean sendToDesktop) {
        this.sendToDesktop = sendToDesktop;
    }

    public boolean isSendToDesktop() {
        return sendToDesktop;
    }


    public void setUpLoadConfig(UpLoadConfig upLoadConfig) {
        this.upLoadConfig = upLoadConfig;
    }

    public UpLoadConfig getUpLoadConfig() {
        return upLoadConfig;
    }

    public Boolean getAutoUpload() {
        return autoUpload;
    }

    public void setAutoUpload(Boolean autoUpload) {
        this.autoUpload = autoUpload;
    }

    public List<String> getSelectIps() {
        if (selectIps == null) {
            return null;
        }
        return Collections.unmodifiableList(selectIps);
    }

    public List<String> getSelectHosts() {
        if (selectHosts == null) {
            return null;
        }
        return Collections.unmodifiableList(selectHosts);
    }

    public String getSelectPG() {
        return selectPG;
    }

    public void setSelectIps(ArrayList<String> selectIps) {
        this.selectIps = selectIps;
    }

    public void setSelectHosts(ArrayList<String> selectHosts) {
        this.selectHosts = selectHosts;
    }

    public void setSelectPG(String selectPG) {
        this.selectPG = selectPG;
    }

    public void setVerifyUID(boolean verifyUID) {
        mVerifyUID = verifyUID;
    }

    private ProxyConfig() {

    }


    public String getSessionName() {
        if (mSessionName == null) {
            mSessionName = "Easy Firewall";
        }
        return mSessionName;
    }

    public int getMTU() {
        if (mMtu > 1400 && mMtu <= 20000) {
            return mMtu;
        } else {
            return 20000;
        }
    }


    public void registerVpnStatusListener(VpnStatusListener vpnStatusListener) {
        mVpnStatusListeners.add(vpnStatusListener);
    }

    public void unregisterVpnStatusListener(VpnStatusListener vpnStatusListener) {
        mVpnStatusListeners.remove(vpnStatusListener);
    }

    public void onVpnStart(Context context) {
        VpnStatusListener[] vpnStatusListeners = new VpnStatusListener[mVpnStatusListeners.size()];
        mVpnStatusListeners.toArray(vpnStatusListeners);
        for (VpnStatusListener listener : vpnStatusListeners) {
            listener.onVpnStart(context);
        }
        clearCaptureVideo();
        mDaoSession = SessionHelper.getDaoSession(context,SessionHelper.getDbName(CaptureVpnService.lastVpnStartTimeFormat));
    }

    private synchronized void  clearCaptureVideo() {
        captureVideo.clear();
    }

    public DaoSession getCurrentDaoSession(){
        return mDaoSession;
    }


    public void onVpnEnd(Context context) {
        VpnStatusListener[] vpnStatusListeners = new VpnStatusListener[mVpnStatusListeners.size()];
        mVpnStatusListeners.toArray(vpnStatusListeners);
        for (VpnStatusListener listener : vpnStatusListeners) {
            listener.onVpnEnd(context);
        }
    }

    public IPAddress getDefaultLocalIP() {
        return new IPAddress("10.8.0.2", 32);
    }

    public void setAutoMatchHost(boolean autoMatch) {
        this.autoMatchHost = autoMatch;
    }

    public boolean isAutoMatchHost() {
        return autoMatchHost;
    }

    public boolean isSaveUdp() {
        return isSaveUdp;
    }

    public void setSaveUdp(boolean saveUdp) {
        isSaveUdp = saveUdp;
    }

    public String getPGName() {
        if (pgName == null) {
            if (VpnServiceHelper.getContext() != null) {
                pgName = VpnServiceHelper.getContext().getPackageName();
            }
        }
        return pgName;
    }

    public boolean verifyUID() {
        return mVerifyUID;
    }

    @NotNull
    public synchronized ArrayList<String> getCurrentCaptureVideo() {
       return new ArrayList<>(captureVideo);
    }
    public synchronized void onVideoCaptured(String path){
        captureVideo.add(path);
    }
    @Keep
    public interface VpnStatusListener {
        void onVpnStart(Context context);

        void onVpnEnd(Context context);
    }

    public static class IPAddress {
        public final String Address;
        public final int PrefixLength;

        public IPAddress(String address, int prefixLength) {
            Address = address;
            PrefixLength = prefixLength;
        }

        public IPAddress(String ipAddressString) {
            String[] arrStrings = ipAddressString.split("/");
            String address = arrStrings[0];
            int prefixLength = 32;
            if (arrStrings.length > 1) {
                prefixLength = Integer.parseInt(arrStrings[1]);
            }

            this.Address = address;
            this.PrefixLength = prefixLength;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof IPAddress)) {
                return false;
            } else {
                return this.toString().equals(o.toString());
            }
        }

        @Override
        public String toString() {
            return String.format("%s/%d", Address, PrefixLength);
        }
    }
}
