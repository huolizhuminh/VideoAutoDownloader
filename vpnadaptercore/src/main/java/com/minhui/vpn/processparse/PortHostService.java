package com.minhui.vpn.processparse;

import androidx.annotation.Keep;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/5.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */
@Keep

public class PortHostService /*extends Service*/ {
 /*   private static final String ACTION = "action";
    private static final String TAG = "PortHostService";
    private static PortHostService instance;
    static ConcurrentHashMap<String, String> map;
    public static final int REFRESH_TCP = 1;
    public static final int REFRESH_UDP = 2;
    public static final int REFRESH_ALL = 0;
    private boolean[] states = {false, false, false, false, false, false};

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NetFileManagerDumper.getInstance().init(getApplicationContext());
        try {
            map = (ConcurrentHashMap) ACache.get(VpnServiceHelper.getContext()).getAsObject(VPNConstants.HOST_APP_MAP);
        } catch (Exception e) {
            VPNLog.e(TAG, "failed get map data");
        }
        if (map == null) {
            map = new ConcurrentHashMap<>(512);
        }
        instance = this;
    }

    public static PortHostService getInstance() {
        return instance;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public static void saveData() {
        if (instance == null) {
            return;
        }
        ACache.get(VpnServiceHelper.getContext()).put(VPNConstants.HOST_APP_MAP, map);
    }


    public static void refreshTCPSessionInfo() {
        if (instance == null) {
            return;
        }
        List<NatSession> allSession = getNeedRefreshSession(NatSession.TCP);
        if (allSession == null || allSession.isEmpty()) {
            return;
        }
        instance.refreshSession(NatSessionManager.getAllSession());
    }

    private static List<NatSession> getNeedRefreshSession(String type) {
        List<NatSession> netConnections = NatSessionManager.getAllSession();
        boolean needRefresh = false;
        for (NatSession connection : netConnections) {
            if (connection == null) {
                continue;
            }
            if (connection.appInfo == null && type.equals(connection.getNetType())) {
                needRefresh = true;
                break;
            }
        }
        if (!needRefresh) {
            return null;
        }
        return netConnections;
    }


    private void refreshSession(List<NatSession> netConnections) {
        if (netConnections == null || netConnections.isEmpty()) {
            VPNLog.d(TAG, "refreshSession is empty");
            return;
        }
        for (int i = 0; i < NetFileManagerDumper.TCP_INDEX.length; i++) {
            refreshSessionAsync(NetFileManagerDumper.TCP_INDEX[i], netConnections);
        }
    }

    public void refreshSessionAsync(final int type, final List<NatSession> netConnections) {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                refreshSession(type, netConnections);
            }
        });
    }

    private void refreshSession(int type, List<NatSession> netConnections) {
        refreshWithNetFile(netConnections, type);
    }

    private void refreshWithNetFile(List<NatSession> netConnections, int type) {
        try {
            if (states[type]) {
                return;
            }
            states[type] = true;
            NetFileManagerDumper.getInstance().refresh(type);
            for (NatSession connection : netConnections) {
                if (connection.isUDP() && ProxyConfig.Instance.isSaveUdp()) {
                    refreshUDPWithNetFile(connection);
                } else {
                    refreshTCPWithNetFile(connection);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            VPNLog.d(TAG, "failed to refreshAllSessionInfo " + e.getMessage());
        } finally {
            states[type] = false;
        }
    }

    private void refreshTCPWithNetFile(NatSession connection) {
        if (connection.appInfo != null) {
            return;
        }
        int searchPort = connection.localPort & 0XFFFF;
        Integer uid = NetFileManagerDumper.getInstance().getUid(searchPort);
        AppInfo appInfo = null;
        if (uid != null) {
            appInfo = AppInfo.createFromUid(VpnServiceHelper.getContext(), uid);
        }
        String remoteHost = connection.getRemoteHost();
        if (appInfo != null && uid != 0) {
            map.put(remoteHost, appInfo.pkgs.getAt(0));
            if (Utils.ispv4(remoteHost)) {
                map.put(connection.getSimpleRemoteHost(), appInfo.pkgs.getAt(0));
            }
            connection.appInfo = appInfo;
        } else if (connection.defaultAPP == null) {

            String cacheAppInfo = map.get(remoteHost);
            String simpleCacheAppInfo = map.get(connection.getSimpleRemoteHost());
            if (cacheAppInfo != null) {
                connection.defaultAPP = cacheAppInfo;

            } else if (simpleCacheAppInfo != null) {
                connection.defaultAPP = simpleCacheAppInfo;
            }
            if (connection.defaultAPP != null) {
                connection.defaultAPPName = AppInfo.getAppName(this, connection.defaultAPP);
            }

        }
    }


    public static void startParse(Context context) {
        Intent intent = new Intent(context, PortHostService.class);
        context.startService(intent);
    }

    public static void stopParse(Context context) {
        Intent intent = new Intent(context, PortHostService.class);
        context.stopService(intent);
    }

    public static String getDefaultApp(String remoteHost) {
        if (remoteHost == null) {
            return null;
        }
        if (map == null) {
            return null;
        } else {
            return map.get(remoteHost);
        }

    }

    public static void refreshUDPWithNetFile(final NatSession session) {
        if (session == null) {
            return;
        }
        if (!CaptureVpnService.isDNSIP(session.getRemoteIPStr())) {
            return;
        }
        if (session.udpRequest != null) {
            session.defaultAPP = map.get(session.udpRequest);
            return;
        }
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (session.udpRequest == null) {
                    Conversation conversation = session.getLastConversation();
                    List<ShowData> newShowDataList = SaveDataParseHelper.getValidShowDataFromDir(conversation, true);
                    if (newShowDataList == null || newShowDataList.isEmpty()) {
                        return;
                    }
                    ShowData showData = newShowDataList.get(0);
                    session.udpRequest = showData.getAndRefreshShowStr();
                }
                if (session.udpRequest == null) {
                    return;
                }
                VPNLog.d(TAG, "refreshUDPSessionInfo body " + session.udpRequest);
                session.defaultAPP = map.get(session.udpRequest);
            }
        });
    }*/
}
