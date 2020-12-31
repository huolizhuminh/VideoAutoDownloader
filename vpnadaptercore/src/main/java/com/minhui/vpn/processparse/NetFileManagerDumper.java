package com.minhui.vpn.processparse;

import android.content.Context;

import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.nat.NatSessionManager;
import com.minhui.vpn.utils.Utils;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static android.os.Process.INVALID_UID;

public class NetFileManagerDumper implements UIDDumper {
    public final static int TYPE_MAX = 6;
    private static final String TAG = "NetFileManager";
    private final static int DATA_LOCAL = 2;
    private final static int DATA_REMOTE = 3;
    private final static int DATA_UID = 8;
    private static final boolean LOG_DEBUG = true;
    private Map<Integer, Integer> processHost = new ConcurrentHashMap<>();
    private File[] mFileArray;
    private StringBuilder sbBuilder = new StringBuilder();
    private Context context;
    private static final String PATH_TCP = "/proc/net/tcp";
    private static final String PATH_TCP6 = "/proc/net/tcp6";
    private static final String PATH_RAW = "/proc/net/raw";
    private static final String PATH_RAW6 = "/proc/net/raw6";
    private static final String PATH_UDP = "/proc/net/udp";
    private static final String PATH_UDP6 = "/proc/net/udp6";
    private long[] mLastParseTimeArray =new long[TYPE_MAX];
    public static final int[] TCP_INDEX = {0, 1, 2, 3};
    public static final int[] UPD_INDEX = {4, 5};

    @Override
    public void init(Context context) {
        this.context = context;

        mFileArray = new File[TYPE_MAX];
        mFileArray[0] = new File(PATH_TCP);
        mFileArray[1] = new File(PATH_TCP6);
        mFileArray[2] = new File(PATH_RAW);
        mFileArray[3] = new File(PATH_RAW6);
        mFileArray[4] = new File(PATH_UDP);
        mFileArray[5] = new File(PATH_UDP6);
    }

    @Override
    public int getUid(Context context, NatSession session) {
        int[] refreshFileIndex;
        if (NatSession.TCP.equals(session.netType)) {
            refreshFileIndex = TCP_INDEX;
        } else {
            refreshFileIndex = UPD_INDEX;
        }
        for (int i = 0; i < refreshFileIndex.length; i++) {
            parseFileAndRefresh(refreshFileIndex[i]);
        }
        Integer uid = processHost.get(session.getLocalPortInt());
        if (uid == null) {
            return INVALID_UID;
        }
        return uid;
    }

    static class InnerClass {
        static NetFileManagerDumper instance = new NetFileManagerDumper();
    }

    public static NetFileManagerDumper getInstance() {
        return InnerClass.instance;
    }

    private int strToInt(String value, int iHex, int iDefault) {
        int iValue = iDefault;
        if (value == null) {
            return iValue;
        }

        try {
            iValue = Integer.parseInt(value, iHex);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return iValue;
    }

    private long strToLong(String value, int iHex, int iDefault) {
        long iValue = iDefault;
        if (value == null) {
            return iValue;
        }

        try {
            iValue = Long.parseLong(value, iHex);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return iValue;
    }

    private NetInfo parseDataNew(String netInfoStr) {
        VPNLog.d(TAG, "parseDataNew start netInfoStr = " + netInfoStr);
        String sSplitItem[] = netInfoStr.split("\\s+");
        String sTmp = null;
        if (sSplitItem.length < 9) {
            VPNLog.d(TAG, "parseDataNew sSplitItem no item ");
            return null;
        } else {
            VPNLog.d(TAG, "parseDataNew sSplitItem item length = " + sSplitItem.length);
        }

        NetInfo netInfo = new NetInfo();

        sTmp = sSplitItem[DATA_LOCAL];
        VPNLog.d(TAG, "parseDataNew local sTmp = " + sTmp);
        String[] sSourceItem = sTmp.split(":");
        if (sSourceItem.length < 2) {
            return null;
        }
        netInfo.setSourPort(strToInt(sSourceItem[1], 16, 0));


        sTmp = sSplitItem[DATA_REMOTE];
        VPNLog.d(TAG, "parseDataNew remote sTmp = " + sTmp);
        String[] sDesItem = sTmp.split(":");
        if (sDesItem.length < 2) {
            return null;
        }
        netInfo.setPort(strToInt(sDesItem[1], 16, 0));
        sTmp = sDesItem[0];
        int len = sTmp.length();
        if (len < 8) {
            return null;
        }
        sTmp = sTmp.substring(len - 8);
        netInfo.setIp(strToLong(sTmp, 16, 0));

        sbBuilder.setLength(0);
        sbBuilder.append(strToInt(sTmp.substring(6, 8), 16, 0))
                .append(".")
                .append(strToInt(sTmp.substring(4, 6), 16, 0))
                .append(".")
                .append(strToInt(sTmp.substring(2, 4), 16, 0))
                .append(".")
                .append(strToInt(sTmp.substring(0, 2), 16, 0));

        sTmp = sbBuilder.toString();
        VPNLog.d(TAG, "parseDataNew address = " + sTmp);
        netInfo.setAddress(sTmp);
        if (sTmp.equals("0.0.0.0")) {
            return null;
        }

        sTmp = sSplitItem[DATA_UID];
        VPNLog.d(TAG, "parseDataNew netInfo = " + netInfo);
        netInfo.setUid(strToInt(sTmp, 10, 0));
        VPNLog.d(TAG, "");
        return netInfo;
    }

    private void saveToMap(NetInfo netInfo, int type) {
        if (netInfo == null) {
            return;
        }
        processHost.put(netInfo.getSourPort(), netInfo.getUid());
    }


    private void log(String tag, String msg) {
        if (LOG_DEBUG) {
            VPNLog.d(TAG, msg);
        }
    }

    private void parseFileAndRefresh(int netType) {
        final long lastParseTime = mLastParseTimeArray[netType];
        try {
            long startTime = System.currentTimeMillis();
            if (netType >= mFileArray.length) {
                return;
            }
            File file = this.mFileArray[netType];
            if (lastParseTime == file.lastModified()) {
                log(TAG, "parseFileAndRefresh hasRefreshed last = " + lastParseTime);
                return;
            }
            BufferedReader bufferedReader = null;

            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                String netInfoStr;

                while ((netInfoStr = bufferedReader.readLine()) != null) {
                    NetInfo netInfo = parseDataNew(netInfoStr);
                    if (netInfo != null) {
                        netInfo.setType(netType);
                        saveToMap(netInfo, netType);
                    }
                }
                 mLastParseTimeArray[netType]=lastParseTime;
            } catch (Exception e) {
                e.printStackTrace();
                Utils.close(bufferedReader);
            }
            List<NatSession> allSession = NatSessionManager.getAllSession();
            for (NatSession session : allSession) {
                if (session == null) {
                    continue;
                }
                if (session.pgName == null) {
                    int localPort = session.getLocalPortInt();
                    Integer uid = processHost.get(localPort);
                    if (uid != null && uid != INVALID_UID) {
                        session.refreshUID(uid);
                    }
                }
            }
            log(TAG, "parseFileAndRefresh " + mFileArray[netType].getName()
                    + " modifyTime " + lastParseTime
                    + " cost time " + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            VPNLog.e(TAG, "error read file " + e.getMessage());
        }
    }
}
