package com.minhui.vpn.nat;

import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.tunnel.ITcpTunnel;
import com.minhui.vpn.parser.CommonMethods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class NatSessionManager {
    /**
     * 会话保存的最大个数
     */

    static final int MAX_SESSION_COUNT = 30;
    /**
     * 会话保存时间
     */

    public static final long SESSION_TIME_OUT_NS = 15 * 1000L;
    private static final ConcurrentHashMap<Short, NatSession> sessions = new ConcurrentHashMap<>();
    private static final String TAG = "NatSessionManager";
    private static final boolean LOG_DEBUG = false;

    /**
     * 通过本地端口获取会话信息
     *
     * @param portKey 本地端口
     * @return 会话信息
     */
    public static NatSession getSession(short portKey) {
        return sessions.get(portKey);
    }

    /**
     * 获取会话个数
     *
     * @return 会话个数
     */
    public static int getSessionCount() {
        return sessions.size();
    }
    
    /**
     * 清除过期的会话
     */
    static void clearExpiredSessions() {
        long now = System.currentTimeMillis();
        Set<Map.Entry<Short, NatSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Short, NatSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Short, NatSession> next = iterator.next();
            ITcpTunnel remoteTunnel = next.getValue().getRemoteTunnel();
            if (now - next.getValue().lastRefreshTime > SESSION_TIME_OUT_NS) {

                if (remoteTunnel != null) {
                    if (!remoteTunnel.hasDepose()) {
                        VPNLog.d(TAG, "has Expired dispose " + next.getValue().getIpAndPort());
                        remoteTunnel.dispose();
                    }
                } else {
                    VPNLog.d(TAG, "has Expired remove " + next.getValue().getIpAndPort());
                    iterator.remove();
                }
                continue;
            }
            if (remoteTunnel != null
                    && remoteTunnel.needCleanSession()) {
                VPNLog.d(TAG, "hasRemove " + (next.getKey() & 0XFFFF));
                iterator.remove();
            }
        }
    }

    private static void log(String tag, String msg) {
        if(LOG_DEBUG){
            VPNLog.d(TAG,msg);
        }
    }

    public static void clearAllSession() {
        sessions.clear();
    }

    public static List<NatSession> getAllSession() {
        ArrayList<NatSession> natSessions = new ArrayList<>();
        Set<Map.Entry<Short, NatSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Short, NatSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Short, NatSession> next = iterator.next();
            NatSession session = next.getValue();
            natSessions.add(session);
        }
        return natSessions;
    }

    public static List<NatSession> getAllTCPSession() {
        ArrayList<NatSession> natSessions = new ArrayList<>();
        Set<Map.Entry<Short, NatSession>> entries = sessions.entrySet();
        Iterator<Map.Entry<Short, NatSession>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Short, NatSession> next = iterator.next();
            NatSession session = next.getValue();
            if (NatSession.TCP.equals(session.getNetType())) {
                natSessions.add(session);
            }

        }
        return natSessions;
    }

    /**
     * 创建会话
     *
     * @param portKey    源端口
     * @param remoteIP   远程ip
     * @param remotePort 远程端口
     * @return NatSession对象
     */
    public static NatSession createSession(short portKey, int sourceIP, int remoteIP, short remotePort, String type) {
        clearExpiredSessions(); //清除过期的会话
        NatSession session = new NatSession();
        // session.lastRefreshTime = System.currentTimeMillis();
        session.sourceIP = sourceIP;
        session.lastRefreshTime = System.currentTimeMillis();
        session.remoteIP = remoteIP;
        session.remotePort = remotePort;
        session.localPort = portKey;
        session.mDaoSession= ProxyConfig.Instance.getCurrentDaoSession();


        if (session.remoteHost == null) {
            session.remoteHost = CommonMethods.ipIntToString(remoteIP);
        }
        session.netType = type;
        session.refreshIpAndPort();
        sessions.put(portKey, session);
        return session;
    }

    public static void removeSession(short portKey) {
        sessions.remove(portKey);
    }


}
