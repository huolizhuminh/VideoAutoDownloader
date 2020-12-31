package com.minhui.vpn.tunnel;


import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.nat.NatSessionManager;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author minhui.zhu
 */
public class TunnelFactory {

    private static final String TAG = "TunnelFactory";
    public static ITcpTunnel wrapLocal(SocketChannel channel, Selector selector,short portKey) {
        ITcpTunnel tunnel;
        NatSession session = NatSessionManager.getSession((short) channel.socket().getPort());
        VPNLog.d(TAG,"wrapLocal "+session.getRemoteHost()+"is https "+session.isHttpsSession +" ishttsrout "+session.isHttpsRoute);
        tunnel = new LocalHttpTunnel( selector,channel,portKey);
        return tunnel;
    }

    public static ITcpTunnel wrapRemote(SocketChannel remoteChannel, Selector mSelector, short portKey) {
        NatSession session = NatSessionManager.getSession(portKey);
        VPNLog.d(TAG,"wrapRemote "+session.getRemoteHost());
        return new RemoteHttpTunnel(mSelector, remoteChannel, portKey);
    }
}
