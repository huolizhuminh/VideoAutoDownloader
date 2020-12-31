package com.minhui.vpn.nat;


import android.os.Process;

import androidx.annotation.Keep;

import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.VpnServiceHelper;
import com.minhui.vpn.greenDao.DaoSession;
import com.minhui.vpn.http.RequestLineParseData;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.parser.ShowDataType;
import com.minhui.vpn.processparse.AppInfo;
import com.minhui.vpn.processparse.DefaultAppManager;
import com.minhui.vpn.tunnel.ITcpTunnel;
import com.minhui.vpn.utils.TimeFormatUtil;
import com.minhui.vpn.utils.VPNDirConstants;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static android.os.Process.INVALID_UID;
import static com.minhui.vpn.parser.TcpDataSaveHelper.REQUEST;
import static com.minhui.vpn.parser.TcpDataSaveHelper.RESPONSE;
import static com.minhui.vpn.utils.VPNDirConstants.BASE_PARSE;
import static com.minhui.vpn.utils.VPNDirConstants.DATA_DIR;
import org.greenrobot.greendao.annotation.Generated;

/**
 *
 */
@Keep
@Entity
public class NatSession implements Serializable {
    @Id(autoincrement = true)
    Long id;
    private static final long serialVersionUID = 4;
    public static final String PARSE_DATA_NAME = "sslCaptureData_";
    public static final String TCP = "TCP";
    public static final String UDP = "UPD";
    private static final String TAG = "NatSession";
    private static final String UNKNOW = "unknow";
    public String netType;
    public String ipAndPort;
    public String sessionTag;
    public int remoteIP;
    public short remotePort;
    public String remoteHost;
    public short localPort;
    /**
     * tcp或UDP数据部分总数据发送量
     */
    public int tcpOrUpdBytesSent;
    /**
     * tcp或UDP数据部分总包发送量
     */
    public int tcpOrUdpPacketSent;
    /**
     * tcp或UDP数据部分总数据接受量
     */

    public int tcpOrUdpReceiveByteNum;
    /**
     * tcp或UDP数据部分总包接受量
     */
    public int tcpOrUdpReceivePacketNum;

    /**
     * 有效数据部分总数据发送量，有效数据不包括https握手的数据
     */
    public int rawBytesSent;
    /**
     * 有效数据数据部分总包发送量，有效数据不包括https握手的数据
     */
    public int rawPacketSent;
    /**
     * 有效数据数据部分总数据接受量，有效数据不包括https握手的数据
     */

    public int rawReceiveByteNum;
    /**
     * 有效数据数据部分总包接受量，有效数据不包括https握手的数据
     */
    public int rawReceivePacketNum;


    public long lastRefreshTime;
    public boolean isHttpsSession;
    public String requestUrl;
    public String pathUrl;
    public String method;
    //包名
    public String pgName;
    //应用名
    public String appName;
    public long connectionStartTime = System.currentTimeMillis();
    public long vpnStartTime;
    public boolean isHttp;

    public String defaultAPP;
    public int sourceIP;
    @Transient
    private transient WeakReference<ITcpTunnel> remoteTunnel;
    @Transient
    private transient WeakReference<ITcpTunnel> localTunnel;
    public boolean isHttpsRoute = false;
    @Transient
    private List<Conversation> conversations = new ArrayList<>();
    @Transient
    private transient ReadWriteLock lock;
    public String defaultAPPName;
    public transient String udpRequest;
    private int mUid = INVALID_UID;
    @Transient
    public transient DaoSession mDaoSession;


    public void initLock() {
        if (lock == null) {
            synchronized (this) {
                if (lock == null) {
                    lock = new ReentrantReadWriteLock();
                }
            }
        }
    }

    private ReadWriteLock getLock() {
        initLock();
        return lock;
    }

    public NatSession() {
        initLock();
    }

    @Generated(hash = 396010789)
    public NatSession(Long id, String netType, String ipAndPort, String sessionTag, int remoteIP,
            short remotePort, String remoteHost, short localPort, int tcpOrUpdBytesSent, int tcpOrUdpPacketSent,
            int tcpOrUdpReceiveByteNum, int tcpOrUdpReceivePacketNum, int rawBytesSent, int rawPacketSent,
            int rawReceiveByteNum, int rawReceivePacketNum, long lastRefreshTime, boolean isHttpsSession,
            String requestUrl, String pathUrl, String method, String pgName, String appName,
            long connectionStartTime, long vpnStartTime, boolean isHttp, String defaultAPP, int sourceIP,
            boolean isHttpsRoute, String defaultAPPName, int mUid) {
        this.id = id;
        this.netType = netType;
        this.ipAndPort = ipAndPort;
        this.sessionTag = sessionTag;
        this.remoteIP = remoteIP;
        this.remotePort = remotePort;
        this.remoteHost = remoteHost;
        this.localPort = localPort;
        this.tcpOrUpdBytesSent = tcpOrUpdBytesSent;
        this.tcpOrUdpPacketSent = tcpOrUdpPacketSent;
        this.tcpOrUdpReceiveByteNum = tcpOrUdpReceiveByteNum;
        this.tcpOrUdpReceivePacketNum = tcpOrUdpReceivePacketNum;
        this.rawBytesSent = rawBytesSent;
        this.rawPacketSent = rawPacketSent;
        this.rawReceiveByteNum = rawReceiveByteNum;
        this.rawReceivePacketNum = rawReceivePacketNum;
        this.lastRefreshTime = lastRefreshTime;
        this.isHttpsSession = isHttpsSession;
        this.requestUrl = requestUrl;
        this.pathUrl = pathUrl;
        this.method = method;
        this.pgName = pgName;
        this.appName = appName;
        this.connectionStartTime = connectionStartTime;
        this.vpnStartTime = vpnStartTime;
        this.isHttp = isHttp;
        this.defaultAPP = defaultAPP;
        this.sourceIP = sourceIP;
        this.isHttpsRoute = isHttpsRoute;
        this.defaultAPPName = defaultAPPName;
        this.mUid = mUid;
    }

    public synchronized List<Conversation> getConversations() {
        if (!isHttp && !isHttpsRoute) {
            return getSocketConversations();
        } else {
            return getHttpAndHttpsConversations();
        }
    }

    private List<Conversation> getHttpAndHttpsConversations() {
        Lock newLock = getLock().readLock();
        newLock.lock();
        ArrayList<Conversation> newConversations;
        if (conversations == null) {
            newConversations = new ArrayList<>();
        } else {
            newConversations = new ArrayList<>(conversations);
        }

        newLock.unlock();
        return newConversations;
    }

    private List<Conversation> getSocketConversations() {
        ArrayList<Conversation> conversations = new ArrayList<>();
        conversations.add(getLastSocketCon());
        return conversations;
    }

    /**
     * @return 表示此链路的数据是否可被解析
     */
    public boolean canParse() {
        return isHttp || isHttpsRoute;
    }

    public boolean isUDP() {
        return NatSession.UDP.equals(netType);
    }

    public void addConversation(String url) {
        Lock writeLock = getLock().writeLock();
        writeLock.lock();
        int index = conversations.size();
        Long conversationSize = getConversationSize(index);

        Conversation conversation = new Conversation
                .Builder()
                .index(index)
                .requestURL(url)
                .session(this)
                .time(System.currentTimeMillis())
                .size(conversationSize)
                .sessionTag(sessionTag)
                .daoSession(mDaoSession)
                .build();
        if (isUDP()) {
            conversation.setType(ShowDataType.UDP);
        } else {
            conversation.setType(ShowDataType.OTHER);
        }
     //   conversation.saveDb();
        conversations.add(conversation);
        writeLock.unlock();
    }

    public void refreshConversation(int index, int type) {
        Lock readLock = this.getLock().readLock();
        readLock.lock();
        Conversation conversation = conversations.get(index);
        readLock.unlock();
        Long conversationSize = getConversationSize(index);
        conversation.setSize(conversationSize);
        conversation.setType(type);
        conversation.refreshDb();
    }

    public ITcpTunnel getRemoteTunnel() {
        if (remoteTunnel != null) {
            return remoteTunnel.get();
        } else {
            return null;
        }
    }

    public void setRemoteTunnel(ITcpTunnel tunnel) {
        remoteTunnel = new WeakReference<ITcpTunnel>(tunnel);
    }

    public ITcpTunnel getLocalTunnel() {
        if (localTunnel == null) {
            return null;
        } else {
            return localTunnel.get();
        }
    }

    public void setLocalTunnel(ITcpTunnel tunnel) {
        localTunnel = new WeakReference<ITcpTunnel>(tunnel);
    }

    @Override
    public String toString() {
        return
                "type='" + netType + '\n' +
                        " ipAndPort=" + ipAndPort + '\n' +
                        " remoteHost=" + remoteHost + '\n' +
                        " tcpOrUpdBytesSent=" + tcpOrUpdBytesSent + '\n' +
                        " tcpOrUdpPacketSent=" + tcpOrUdpPacketSent + '\n' +
                        " tcpOrUdpReceiveByteNum=" + tcpOrUdpReceiveByteNum + '\n' +
                        " tcpOrUdpReceivePacketNum=" + tcpOrUdpReceivePacketNum + '\n' +
                        " rawBytesSent=" + rawBytesSent + '\n' +
                        " rawPacketSent=" + rawPacketSent + '\n' +
                        " rawReceiveByteNum=" + rawReceiveByteNum + '\n' +
                        " rawReceivePacketNum=" + rawReceivePacketNum + '\n' +
                        " lastRefreshTime=" + lastRefreshTime + '\n' +
                        " isHttpsSession=" + isHttpsSession + '\n' +
                        " requestUrl='" + requestUrl + '\n' +
                        " pathUrl=" + pathUrl + '\n' +
                        " method=" + method + '\n' +
                        "connectionStartTime=" + TimeFormatUtil.formatYYMMDDHHMMSS(connectionStartTime) + '\n' +
                        "vpnStartTime=" + TimeFormatUtil.formatYYMMDDHHMMSS(vpnStartTime) + '\n' +
                        "isHttp=" + isHttp + '\n';

    }

    public String getRemoteIPStr() {
        int remoteIPStr1 = (remoteIP & 0XFF000000) >> 24 & 0XFF;
        int remoteIPStr2 = (remoteIP & 0X00FF0000) >> 16;
        int remoteIPStr3 = (remoteIP & 0X0000FF00) >> 8;
        int remoteIPStr4 = remoteIP & 0X000000FF;
        return "" + remoteIPStr1 + "." + remoteIPStr2 + "." + remoteIPStr3 + "." + remoteIPStr4;
    }

    public void refreshIpAndPort() {
        ipAndPort = netType + ":" + getRemoteIPStr() + " re:" + remotePort + " lo:" + getLocalPortInt();
        sessionTag=ipAndPort+"_"+System.currentTimeMillis();
    }

    public String getNetType() {
        return netType;
    }

    public String getIpAndPortDir() {
        return ipAndPort.replace(":", "_").replace(" ", "_");
    }

    public String getIpAndPort() {
        return ipAndPort;
    }

    public int getRemoteIP() {
        return remoteIP;
    }

    public short getRemotePort() {
        return remotePort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public short getLocalPort() {
        return localPort;
    }

    public int getLocalPortInt() {
        return ((int) localPort & 0XFFFF);
    }

    public int getRemotePortInt() {
        return ((int) remotePort & 0XFFFF);
    }

    public int getTcpOrUpdBytesSent() {
        return tcpOrUpdBytesSent;
    }

    public int getTcpOrUdpPacketSent() {
        return tcpOrUdpPacketSent;
    }

    public long getTcpOrUdpReceiveByteNum() {
        return tcpOrUdpReceiveByteNum;
    }

    public long getTcpOrUdpReceivePacketNum() {
        return tcpOrUdpReceivePacketNum;
    }

    public long getRefreshTime() {
        return lastRefreshTime;
    }

    public boolean isHttpsSession() {
        return isHttpsSession;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    public String getMethod() {
        return method;
    }

    public long getConnectionStartTime() {
        return connectionStartTime;
    }

    public long getVpnStartTime() {
        return vpnStartTime;
    }

    private void refreshSessionDb() {
      if(mDaoSession==null){
          return;
      }
      mDaoSession.getNatSessionDao().refresh(this);
    }

    public void deleteCache() {
        if(mDaoSession==null){
            return;
        }
        mDaoSession
                .getNatSessionDao()
                .delete(this);
    }

    /**
     * 判断是否需要抓取udp
     */
    public boolean needCapture() {
        if (!ProxyConfig.Instance.isSaveUdp() && isUDP()) {
            return false;
        }
        String appName = getPGName();
        String sslName = ProxyConfig.Instance.getPGName();
        //不需要抓抓包精灵的包
        if ( sslName.equals(appName)) {
            return false;
        }
        if (sslName.equals(defaultAPP)) {
            return false;
        }

        List<String> ips = ProxyConfig.Instance.getSelectIps();
        List<String> hosts = ProxyConfig.Instance.getSelectHosts();
        //如果没有特殊要求，则所有的都保存
        if (isEmpty(ips) && isEmpty(hosts)) {
            return true;
        }
        if (!isEmpty(ips)) {
            for (String ip : ips) {
                if (ip != null && ip.equals(getRemoteIPStr())) {
                    return true;
                }
            }

        }

        if (!isEmpty(hosts) && remoteHost != null) {
            boolean isAutoMatchHost = ProxyConfig.Instance.isAutoMatchHost();

            for (String host : hosts) {
                if (host == null) {
                    continue;
                }
                if (isAutoMatchHost) {
                    //匹配的逻辑为 remoteHost 有某一个IP中长度等于或大于4的字段
                    if (host.equals(remoteHost)) {
                        return true;
                    }
                    String[] hostSplits = host.split("\\.");
                    for (String hostSplit : hostSplits) {
                        if (hostSplit.length() < 4) {
                            continue;
                        }
                        if (remoteHost.contains(hostSplit)) {
                            return true;
                        }
                    }

                } else {
                    if (host.equals(remoteHost)) {
                        return true;
                    }

                }

            }

        }
        return false;

    }

    private boolean isEmpty(List list) {
        return list == null || list.isEmpty();
    }

    public String getPgName() {
        if (pgName != null) {
           return pgName;
        } else {
           return UNKNOW;
        }
    }

    public String getPGName() {
        if (pgName == null) {
            if (defaultAPP == null) {
                return "UnKnow";
            }
            return defaultAPP;
        } else {
            return pgName;
        }
    }

    public String getSimpleRemoteHost() {

        String[] split = remoteHost.split("\\.");
        return split[split.length - 2] + "." + split[split.length - 1];
    }

    public void refreshRequestData(RequestLineParseData requestLineParseData) {
        method = requestLineParseData.getMethod();
        pathUrl = requestLineParseData.getPathUrl();
        requestUrl = getRequestUrl(pathUrl);

    }

    public String getRequestUrl(String pathUrl) {
        String requestUrl = null;
        if (pathUrl == null) {
            if (remoteHost != null) {
                if (isHttpsSession) {
                    requestUrl = "https://" + remoteHost + ":" + remotePort;
                } else {
                    requestUrl = "http://" + remoteHost + ":" + remotePort;
                }
            }
        } else if (pathUrl.startsWith("/")) {
            if (remoteHost != null) {
                if (isHttpsSession) {
                    requestUrl = "https://" + remoteHost + ":" + remotePort + pathUrl;
                } else {
                    requestUrl = "http://" + remoteHost + ":" + remotePort + pathUrl;
                }
            }
        } else {
            if (pathUrl.startsWith("http")) {
                requestUrl = pathUrl;
            } else {
                if (isHttpsSession) {
                    requestUrl = "http://" + pathUrl;
                } else {
                    requestUrl = "https://" + pathUrl;
                }

            }

        }
        return requestUrl;
    }


    public Conversation getLastConversation() {
        if (isHttpsRoute || isHttp) {
            return getLastHttpOrHttpsCon();
        } else {
            return getLastSocketCon();
        }

    }

    private Conversation getLastSocketCon() {
        long conversationSize = rawBytesSent + rawReceiveByteNum;
        Conversation conversation = new Conversation.Builder()
                .index(0)
                .session(this)
                .requestURL(remoteHost == null ? getRemoteIPStr() : remoteHost)
                .size(conversationSize)
                .time(lastRefreshTime)
                .sessionTag(sessionTag)
                .daoSession(mDaoSession)
                .build();
        conversation.setType(isUDP() ? ShowDataType.UDP : ShowDataType.OTHER);
        return conversation;
    }

    private Conversation getLastHttpOrHttpsCon() {
        if (conversations.isEmpty()) {
            return null;
        }
        return getConversation(conversations.size() - 1);
    }

    public Conversation getConversation(int index) {

        Lock readLock = getLock().readLock();
        readLock.lock();
        if (conversations.size() < index + 1) {
            return null;
        }

        Conversation conversation = conversations.get(index);
        readLock.unlock();
        return conversation;
    }

    public Long getConversationSize(int index) {
        long conversationSize = 0L;
        File requestFile = getReqSaveDataFile(index);
        File responseFile = getRespSaveDataFile(index);
        if (requestFile.exists()) {
            conversationSize = conversationSize + requestFile.length();
        }
        if (responseFile.exists()) {
            conversationSize = conversationSize + responseFile.length();
        }
        return conversationSize;
    }


    public void deleteConversation(int index) {
        Lock writeLock = getLock().writeLock();
        writeLock.lock();
        //将save data 改名
        for (int i = index + 1; i < conversations.size(); i++) {
            File requestFile = getReqSaveDataFile(index);
            requestFile.renameTo(getReqSaveDataFile(index - 1));
            File responseFile = getRespSaveDataFile(index);
            responseFile.renameTo(getRespSaveDataFile(index - 1));
        }
        int conversationSizes = conversations.size();
        if (conversationSizes > index) {
            Conversation conversation = conversations.remove(index);
            conversation.deleteDb();
        }
        writeLock.unlock();
    }

    public byte[] getRemoteIpByte() {
        byte[] ret = new byte[4];
        ret[0] = (byte) ((remoteIP & 0XFF000000) >> 24);
        ret[1] = (byte) ((remoteIP & 0X00FF0000) >> 16);
        ret[2] = (byte) ((remoteIP & 0X0000FF00) >> 8);
        ret[3] = (byte) (remoteIP & 0X000000FF);
        return ret;
    }

    public void refreshUID() {
        if (pgName != null) {
            return;
        }
        mUid = VpnServiceHelper.getUIDDumper().getUid(VpnServiceHelper.getContext(), this);
        if (mUid != INVALID_UID) {
            AppInfo appInfo = AppInfo.createFromUid(VpnServiceHelper.getContext(), mUid);
            if (appInfo != null) {
                refreshAppInfo(appInfo);
            }
        }
    }

    private void refreshAppInfo(AppInfo appInfo) {
        DefaultAppManager.getsInstance().add(remoteHost, appInfo.pkgs.getAt(0));
        pgName = appInfo.pkgs.getAt(0);
        appName = appInfo.leaderAppName;
    }

    public void refreshUID(int uid) {
        if (pgName != null) {
            return;
        }
        if (uid != INVALID_UID) {
            AppInfo appInfo = AppInfo.createFromUid(VpnServiceHelper.getContext(), uid);
            refreshAppInfo(appInfo);
        }
    }

    public void refreshDb() {
        if(mDaoSession==null){
            return;
        }
        if(id==0){
            return;
        }
        mDaoSession
                .getNatSessionDao()
                .update(this);
    }

    public void saveDb() {
        if (mDaoSession == null) {
            return;
        }
        id = mDaoSession.getNatSessionDao().insert(this);
    }

    public void setConversation(List<Conversation> queryConversations) {
        conversations=queryConversations;
    }

    @Keep
    public static class NatSessionComparator implements java.util.Comparator<NatSession> {

        @Override
        public int compare(NatSession o1, NatSession o2) {
            if (o1 == o2) {
                return 0;
            }
            return (int) (o2.lastRefreshTime - o1.lastRefreshTime);
        }
    }

    public String getSaveDataDir() {
        return DATA_DIR
                + TimeFormatUtil.formatYYMMDDHHMMSS(vpnStartTime)
                + "/"
                + getIpAndPortDir();
    }

    public File getReqSaveDataFile(int saveNum) {
        return new File(getSaveDataDir() + "/" + REQUEST + "_" + saveNum);
    }

    public File getRespSaveDataFile(int saveNum) {
        return new File(getSaveDataDir() + "/" + RESPONSE + "_" + saveNum);
    }


    public String getParseDataDir() {
        String packageName = getPgName();
        // String packageName = appInfo == null ? "unknow" : appInfo.pkgs.getAt(0).replace(" ", "_");
        return BASE_PARSE
                + TimeFormatUtil.formatYYMMDDHHMMSS(vpnStartTime)
                + "/"
                + packageName
                + "/"
                + getIpAndPortDir();
    }

    public String getParseDataFile(int index) {
        return getParseDataDir() + "/" + PARSE_DATA_NAME + index + ".txt";
    }

    public String getShowParseDataDir() {
        String packageName = getPgName();
        return "~/sdcard/" + VPNDirConstants.BASE_DIR.replace("/storage/emulated/0/", "") + "ParseData/"
                + TimeFormatUtil.formatYYMMDDHHMMSS(vpnStartTime)
                + "/"
                + packageName
                + "/"
                + getIpAndPortDir();
    }

    @Override
    public int hashCode() {
        return ipAndPort.hashCode();
    }


    @Override
    public boolean equals(Object o) {
        return ipAndPort.equals(((NatSession) o).ipAndPort);
    }

    public boolean isOwnerConn() {
        int myUid = Process.myUid();
        boolean isOwnerConn = (mUid == myUid);
        if (isOwnerConn) {
            VPNLog.d(TAG, "isOwnerConn mUid = " + mUid + " myUid = " + myUid + " host " + remoteHost);
        }
        return isOwnerConn;

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNetType(String netType) {
        this.netType = netType;
    }

    public void setIpAndPort(String ipAndPort) {
        this.ipAndPort = ipAndPort;
    }

    public String getSessionTag() {
        return this.sessionTag;
    }

    public void setSessionTag(String sessionTag) {
        this.sessionTag = sessionTag;
    }

    public void setRemoteIP(int remoteIP) {
        this.remoteIP = remoteIP;
    }

    public void setRemotePort(short remotePort) {
        this.remotePort = remotePort;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public void setLocalPort(short localPort) {
        this.localPort = localPort;
    }

    public void setTcpOrUpdBytesSent(int tcpOrUpdBytesSent) {
        this.tcpOrUpdBytesSent = tcpOrUpdBytesSent;
    }

    public void setTcpOrUdpPacketSent(int tcpOrUdpPacketSent) {
        this.tcpOrUdpPacketSent = tcpOrUdpPacketSent;
    }

    public void setTcpOrUdpReceiveByteNum(int tcpOrUdpReceiveByteNum) {
        this.tcpOrUdpReceiveByteNum = tcpOrUdpReceiveByteNum;
    }

    public void setTcpOrUdpReceivePacketNum(int tcpOrUdpReceivePacketNum) {
        this.tcpOrUdpReceivePacketNum = tcpOrUdpReceivePacketNum;
    }

    public int getRawBytesSent() {
        return this.rawBytesSent;
    }

    public void setRawBytesSent(int rawBytesSent) {
        this.rawBytesSent = rawBytesSent;
    }

    public int getRawPacketSent() {
        return this.rawPacketSent;
    }

    public void setRawPacketSent(int rawPacketSent) {
        this.rawPacketSent = rawPacketSent;
    }

    public int getRawReceiveByteNum() {
        return this.rawReceiveByteNum;
    }

    public void setRawReceiveByteNum(int rawReceiveByteNum) {
        this.rawReceiveByteNum = rawReceiveByteNum;
    }

    public int getRawReceivePacketNum() {
        return this.rawReceivePacketNum;
    }

    public void setRawReceivePacketNum(int rawReceivePacketNum) {
        this.rawReceivePacketNum = rawReceivePacketNum;
    }

    public long getLastRefreshTime() {
        return this.lastRefreshTime;
    }

    public void setLastRefreshTime(long lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }

    public boolean getIsHttpsSession() {
        return this.isHttpsSession;
    }

    public void setIsHttpsSession(boolean isHttpsSession) {
        this.isHttpsSession = isHttpsSession;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public void setPathUrl(String pathUrl) {
        this.pathUrl = pathUrl;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPgName(String pgName) {
        this.pgName = pgName;
    }

    public String getAppName() {
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setConnectionStartTime(long connectionStartTime) {
        this.connectionStartTime = connectionStartTime;
    }

    public void setVpnStartTime(long vpnStartTime) {
        this.vpnStartTime = vpnStartTime;
    }

    public boolean getIsHttp() {
        return this.isHttp;
    }

    public void setIsHttp(boolean isHttp) {
        this.isHttp = isHttp;
    }

    public String getDefaultAPP() {
        return this.defaultAPP;
    }

    public void setDefaultAPP(String defaultAPP) {
        this.defaultAPP = defaultAPP;
    }

    public int getSourceIP() {
        return this.sourceIP;
    }

    public void setSourceIP(int sourceIP) {
        this.sourceIP = sourceIP;
    }

    public boolean getIsHttpsRoute() {
        return this.isHttpsRoute;
    }

    public void setIsHttpsRoute(boolean isHttpsRoute) {
        this.isHttpsRoute = isHttpsRoute;
    }

    public String getDefaultAPPName() {
        return this.defaultAPPName;
    }

    public void setDefaultAPPName(String defaultAPPName) {
        this.defaultAPPName = defaultAPPName;
    }

    public int getMUid() {
        return this.mUid;
    }

    public void setMUid(int mUid) {
        this.mUid = mUid;
    }

}
