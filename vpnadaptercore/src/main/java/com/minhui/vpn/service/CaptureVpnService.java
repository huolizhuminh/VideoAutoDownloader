package com.minhui.vpn.service;

import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;

import androidx.annotation.Keep;

import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.VpnServiceHelper;
import com.minhui.vpn.http.HttpRequestHeaderParser;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.ConversationManager;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.nat.NatSessionManager;
import com.minhui.vpn.parser.CommonMethods;
import com.minhui.vpn.processparse.DefaultAppManager;
import com.minhui.vpn.proxy.TcpProxyServer;
import com.minhui.vpn.tcpip.IPHeader;
import com.minhui.vpn.tcpip.TCPHeader;
import com.minhui.vpn.tunnel.ITcpTunnel;
import com.minhui.vpn.udpip.Packet;
import com.minhui.vpn.tunnel.UDPServer;
import com.minhui.vpn.utils.ThreadProxy;
import com.minhui.vpn.utils.TimeFormatUtil;
import com.minhui.vpn.utils.VPNDirConstants;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.minhui.vpn.utils.VPNDirConstants.BASE_PARSE;

@Keep
public class CaptureVpnService extends VpnService implements Runnable {
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    private static final String GOOGLE_DNS_FIRST = "8.8.8.8";
    private static final String GOOGLE_DNS_SECOND = "8.8.4.4";
    private static final String AMERICA = "208.67.222.222";
    private static final String CHINA_DNS_FIRST = "114.114.114.114";
    public static final String[] DNS_IPS = {"8.8.8.8", "8.8.4.4", "208.67.222.222", "114.114.114.114"};
    private static final String TAG = "CaptureVpnService";
    private static final int FOR_GROUND_ID = 101;
    private static final boolean LOG_DEBUG = false;
    private static int ID;
    private static int LOCAL_IP;
    private boolean isRunning = false;
    private Thread mVPNThread;
    private ParcelFileDescriptor mVPNInterface;
    private TcpProxyServer mTcpProxyServer;
    private FileOutputStream mVPNOutputStream;
    private ConcurrentLinkedQueue<ByteBuffer> outputQueue;
    private FileInputStream in;
    private UDPServer udpServer;
    private String selectPackage;
    public static final int MUTE_SIZE = 5120;
    public static long vpnStartTime;
    public static String lastVpnStartTimeFormat = null;
    private FileChannel inputChannel;
    Handler handler;

    public CaptureVpnService() {
        ID++;
        handler = new Handler(Looper.getMainLooper());
        log(TAG, "New VPNService \n" + ID);
    }

    private void log(String tag, String msg) {
        if (LOG_DEBUG) {
            VPNLog.d(TAG, msg);
        }
    }

    public static boolean isDNSIP(String ip) {
        for (String dns : DNS_IPS) {
            if (dns.equals(ip)) {
                return true;
            }
        }
        return false;
    }

    //启动Vpn工作线程
    @Override
    public void onCreate() {
        log(TAG, "VPNService  created.\n" + ID);
        VpnServiceHelper.onVpnServiceCreated(this);
        mVPNThread = new Thread(this, "VPNServiceThread");
        mVPNThread.start();
        setVpnRunningStatus(true);
        ConversationManager.getInstance().init(getApplicationContext());
        //   startForeground(FOR_GROUND_ID,new Notification());
        //   notifyStatus(new VPNEvent(VPNEvent.Status.STARTING));
        super.onCreate();
    }

    //只设置IsRunning = true;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    //停止Vpn工作线程
    @Override
    public void onDestroy() {
        VPNLog.i("VPNService(%s) destroyed  " + ID);
        if (mVPNThread != null) {
            mVPNThread.interrupt();
        }
        VpnServiceHelper.onVpnServiceDestroy();
        super.onDestroy();
    }


    //建立VPN，同时监听出口流量
    private void runVPN() throws Exception {

        this.mVPNInterface = establishVPN();
        startStream();
    }

    private void startStream() throws Exception {
        int size = 0;
        mVPNOutputStream = new FileOutputStream(mVPNInterface.getFileDescriptor());
        in = new FileInputStream(mVPNInterface.getFileDescriptor());
        inputChannel = in.getChannel();
        boolean hasReceive = false;
        boolean dataSent = true;
        ByteBuffer inputPacket = null;
        while (isRunning) {
            if (dataSent) {
                inputPacket = ByteBuffer.allocate(MUTE_SIZE);
            } else {
                inputPacket.clear();
            }

            size = inputChannel.read(inputPacket);
            //   final byte[] mPacket = new byte[MUTE_SIZE];
            //   size = in.read(inputPacket.array());
            if (size > 0) {
                dataSent = true;
                if (mTcpProxyServer.stopped) {
                    in.close();
                    throw new Exception("LocalServer stopped.");
                }
                inputPacket.flip();
                final ByteBuffer finalPacket = inputPacket;
                ThreadProxy.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            onIPPacketReceived(finalPacket);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


            } else {
                dataSent = false;
            }

            ByteBuffer outputPacket = outputQueue.poll();
            if (outputPacket != null) {
                hasReceive = true;
                mVPNOutputStream.write(outputPacket.array(), 0, outputPacket.limit());
            } else {
                hasReceive = false;
            }

            if (!dataSent && !hasReceive) {
                //    log(TAG,"sleep");
                Thread.sleep(10);
            }

        }
        in.close();
        disconnectVPN();
    }

    void onIPPacketReceived(ByteBuffer mPacket) throws IOException {
        IPHeader ipHeader = new IPHeader(mPacket.array(), 0);
        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                onTcpPacketReceived(mPacket);

                break;
            case IPHeader.UDP:
                onUdpPacketReceived(mPacket);


                break;
            default:
                break;
        }


    }

    private void onUdpPacketReceived(ByteBuffer mPacket) throws UnknownHostException {
        IPHeader ipHeader = new IPHeader(mPacket.array(), 0);
     /*   UDPHeader tcpHeader = new UDPHeader(ipHeader.mData, 20);
        short portKey = tcpHeader.getSourcePort();*/
        Packet packet = new Packet(mPacket);
        short portKey = packet.udpHeader.sourcePort;
        short destinationProt = packet.udpHeader.destinationPort;
        NatSession session = NatSessionManager.getSession(portKey);
        if (session == null || session.remoteIP != ipHeader.getDestinationIP() || session.remotePort
                != destinationProt) {
            session = NatSessionManager.createSession(portKey, ipHeader.getSourceIP(), ipHeader.getDestinationIP(),
                    destinationProt, NatSession.UDP);
            session.vpnStartTime = vpnStartTime;
            final NatSession finalSession = session;
          /*  if (finalSession.appInfo == null && ProxyConfig.Instance.isSaveUdp()) {
                PortHostService.refreshUDPSessionInfo(session);
            }*/
        }

        session.lastRefreshTime = System.currentTimeMillis();
        session.tcpOrUdpPacketSent++; //注意顺序

        udpServer.processUDPPacket(packet, portKey);
    }

    private void onTcpPacketReceived(ByteBuffer buffer) throws IOException {
        IPHeader ipHeader = new IPHeader(buffer.array(), 0);
        TCPHeader tcpHeader = new TCPHeader(buffer.array(), 20);
        //矫正TCPHeader里的偏移量，使它指向真正的TCP数据地址
        tcpHeader.mOffset = ipHeader.getHeaderLength();
        if (tcpHeader.getSourcePort() == mTcpProxyServer.port) {
            NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
            if (session != null) {

                ipHeader.setSourceIP(ipHeader.getDestinationIP());
                tcpHeader.setSourcePort(session.remotePort);
                ipHeader.setDestinationIP(LOCAL_IP);

                CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                buffer.position(buffer.limit());
                outputQueue.offer(buffer);
                session.tcpOrUdpReceiveByteNum += buffer.limit();
                log(TAG + ((int) session.getLocalPort() & 0XFFFF), "process  tcp packet from net ");
                session.tcpOrUdpReceivePacketNum++;
                session.lastRefreshTime = System.currentTimeMillis();
            } else {
                log(TAG, "NoSession: \n" + ipHeader.toString() + tcpHeader.toString());
            }

        } else {
            //添加端口映射
            short portKey = tcpHeader.getSourcePort();
            NatSession session = NatSessionManager.getSession(portKey);

            if (session == null || session.remoteIP != ipHeader.getDestinationIP() || session.remotePort
                    != tcpHeader.getDestinationPort()) {
                session = NatSessionManager.createSession(portKey, ipHeader.getSourceIP(), ipHeader.getDestinationIP(), tcpHeader
                        .getDestinationPort(), NatSession.TCP);
                session.vpnStartTime = vpnStartTime;

            }
            log(TAG + ((int) session.getLocalPort() & 0XFFFF), "process  tcp packet to net ");
            //  session.lastRefreshTime = System.currentTimeMillis();
            session.tcpOrUdpPacketSent++; //注意顺序
            int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
            //丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
            if (session.tcpOrUdpPacketSent == 2 && tcpDataSize == 0) {
                return;
            }

            //分析数据，找到host
            if (session.tcpOrUpdBytesSent == 0 && tcpDataSize > 10) {
                int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();
                HttpRequestHeaderParser.parseHttpRequestHeader(session, tcpHeader.mData, dataOffset,
                        tcpDataSize);
                log(TAG, "Host:\n" + session.remoteHost);
                log(TAG, "Request: \n" + session.method + session.requestUrl);
                if (session.pgName == null) {
                    session.refreshUID();
                }

            } else if (session.tcpOrUpdBytesSent > 0
                    && !session.isHttpsSession
                    && session.isHttp
                    && session.remoteHost == null
                    && session.requestUrl == null) {
                int dataOffset = tcpHeader.mOffset + tcpHeader.getHeaderLength();
                session.remoteHost = HttpRequestHeaderParser.getRemoteHost(tcpHeader.mData, dataOffset,
                        tcpDataSize);
                session.requestUrl = "http://" + session.remoteHost + "/" + session.pathUrl;
                if (session.pgName == null) {
                    session.refreshUID();
                }

            }
            if (session.getRemotePortInt() == 443 && !session.isHttpsSession) {
                ITcpTunnel remoteTunnel = session.getRemoteTunnel();
                if (remoteTunnel != null) {
                    remoteTunnel.dispose();
                }
            }
            //转发给本地TCP服务器
            ipHeader.setSourceIP(ipHeader.getDestinationIP());
            ipHeader.setDestinationIP(LOCAL_IP);
            tcpHeader.setDestinationPort(mTcpProxyServer.port);

            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
            buffer.position(buffer.limit());
            outputQueue.offer(buffer);
            //   mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
            //注意顺序
            session.tcpOrUpdBytesSent += tcpDataSize;
        }
    }

    private void waitUntilPrepared() {

        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

                log(TAG, "waitUntilPrepared catch an exception %s\n" + e.getMessage());
            }
        }
    }

    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(MUTE_SIZE);
        selectPackage = ProxyConfig.Instance.getSelectPG();
        log(TAG, "setMtu: \n" + ProxyConfig.Instance.getMTU());

        ProxyConfig.IPAddress ipAddress = ProxyConfig.Instance.getDefaultLocalIP();
        LOCAL_IP = CommonMethods.ipStringToInt(ipAddress.Address);
        builder.addAddress(ipAddress.Address, ipAddress.PrefixLength);
        log(TAG, "addAddress: %s/%d\n" + ipAddress.Address + ipAddress.PrefixLength);

        builder.addRoute(VPN_ROUTE, 0);

        for (String ip : DNS_IPS) {
            builder.addDnsServer(ip);
        }
        try {
            if (selectPackage != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.addAllowedApplication(selectPackage);
                    builder.addAllowedApplication(getPackageName());
                }
            }
        } catch (Exception e) {
            VPNLog.e(TAG, "failed to establishVPN msg = " + e.getMessage());
        }

        builder.setSession(VpnServiceHelper.getName());
        ParcelFileDescriptor pfdDescriptor = builder.establish();
        //  notifyStatus(new VPNEvent(VPNEvent.Status.ESTABLISHED));
        return pfdDescriptor;
    }

    @Override
    public void run() {
        try {
            log(TAG, "VPNService(%s) work thread is Running..." + ID);
            vpnStartTime = System.currentTimeMillis();
            lastVpnStartTimeFormat = TimeFormatUtil.formatYYMMDDHHMMSS(vpnStartTime);
            waitUntilPrepared();
            outputQueue = new ConcurrentLinkedQueue<>();

            //启动TCP代理服务
            mTcpProxyServer = new TcpProxyServer(0);
            mTcpProxyServer.start();
            udpServer = new UDPServer(this, outputQueue);
            udpServer.start();
            NatSessionManager.clearAllSession();
            log(TAG, "DnsProxy started.");
            ConversationManager.getInstance().clear();
            ProxyConfig.Instance.onVpnStart(this);
            while (isRunning) {
                runVPN();
            }


        } catch (InterruptedException e) {

            VPNLog.e(TAG, "VpnService run catch an exception " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            VPNLog.e(TAG, "VpnService run catch an exception " + e.getMessage());
            e.printStackTrace();
        } finally {
            log(TAG, "VpnService terminated");
            ProxyConfig.Instance.onVpnEnd(this);
            dispose();
        }
    }

    public void disconnectVPN() {
        try {
            if (mVPNInterface != null) {
                mVPNInterface.close();
                mVPNInterface = null;
            }
        } catch (Exception e) {
            VPNLog.e(TAG, "failed to disconnectVPN msg = " + e.getMessage());
            //ignore
        }
        // notifyStatus(new VPNEvent(VPNEvent.Status.UNESTABLISHED));
        this.mVPNOutputStream = null;
    }

    private synchronized void dispose() {
        try {
            //断开VPN
            disconnectVPN();
        } catch (Exception e) {

        }
        try {
            //停止TCP代理服务
            if (mTcpProxyServer != null) {
                mTcpProxyServer.stop();
                mTcpProxyServer = null;
                log(TAG, "TcpProxyServer stopped.");
            }

        } catch (Exception e) {

        }
        try {
            udpServer.closeAllUDPConn();
        } catch (Exception e) {

        }
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                DefaultAppManager.getsInstance().saveData();
            }
        });


        stopSelf();
        setVpnRunningStatus(false);

    }


    public boolean vpnRunningStatus() {
        return isRunning;
    }

    public void setVpnRunningStatus(boolean isRunning) {
        this.isRunning = isRunning;
    }
}
