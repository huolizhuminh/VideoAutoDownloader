package com.minhui.vpn.proxy;


import com.minhui.vpn.BuildConfig;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.tunnel.KeyHandler;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.NatSessionManager;
import com.minhui.vpn.tunnel.ITcpTunnel;
import com.minhui.vpn.tunnel.TunnelFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TcpProxyServer implements Runnable {
    private static final String TAG = "TcpProxyServer";
    private static final boolean DEBUG_LOG = BuildConfig.DEBUG;
    private static final boolean LOG_DEBUG = false;
    public boolean stopped;
    public short port;

    private Selector mSelector;
    private ServerSocketChannel mServerSocketChannel;

    public TcpProxyServer(int port) throws IOException {
        mSelector = Selector.open();
        mServerSocketChannel = ServerSocketChannel.open();
        mServerSocketChannel.configureBlocking(false);
        mServerSocketChannel.socket().bind(new InetSocketAddress(port));
        mServerSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);
        this.port = (short) mServerSocketChannel.socket().getLocalPort();

        log(TAG, "AsyncTcpServer listen on %s:%d success.\n" + mServerSocketChannel.socket().getInetAddress()
                .toString() + (this.port & 0xFFFF));
    }

    /**
     * 启动TcpProxyServer线程
     */
    public void start() {
        Thread mServerThread = new Thread(this, "TcpProxyServerThread");
        mServerThread.start();
    }

    public void stop() {
        this.stopped = true;
    }

    private void log(String tag, String msg) {
        if (LOG_DEBUG) {
            VPNLog.d(TAG, msg);
        }
    }

    @Override
    public void run() {

        while (!stopped) {
            try {
                //
                mSelector.select();
                Set<SelectionKey> selectionKeys = mSelector.selectedKeys();
                if (selectionKeys == null || selectionKeys.size() == 0) {
                    refreshAllSessionKey();
                    Thread.sleep(5);
                    continue;
                }
                long startHandlerTime = System.currentTimeMillis();
                int size = selectionKeys.size();
                Iterator<SelectionKey> keyIterator = mSelector.selectedKeys().iterator();
                boolean hasInValisKey = false;
                while (keyIterator.hasNext()) {
                    final SelectionKey key = keyIterator.next();
                    if (key.isValid()) {
                        hasInValisKey = true;
                        if (key.isAcceptable()) {
                            log(TAG, "isAcceptable");
                            onAccepted(key);
                        } else {
                            final Object attachment = key.attachment();
                            if (attachment instanceof KeyHandler) {
                                ((KeyHandler) attachment).onKeyReady(key);

                            }
                        }
                    }
                    keyIterator.remove();
                }
                refreshAllSessionKey();
                if (!hasInValisKey) {
                    Thread.sleep(5);
                } else {
                    log(TAG, "handletime " + (System.currentTimeMillis() - startHandlerTime) + " size:" + size);
                }

            } catch (Exception e) {

                VPNLog.e(TAG, "updServer catch an exception: %s" + e.getMessage());
            }
        }
        if (mSelector != null) {
            try {
                mSelector.close();
                mSelector = null;
            } catch (Exception ex) {

                VPNLog.e(TAG, "TcpProxyServer mSelector.close() catch an exception:" + ex.getMessage());
            }
        }

        if (mServerSocketChannel != null) {
            try {
                mServerSocketChannel.close();
                mServerSocketChannel = null;
            } catch (Exception ex) {
                VPNLog.e("TcpProxyServer mServerSocketChannel.close() catch an exception:" + ex.getMessage());
            }
        }

    }

    private void refreshAllSessionKey() {
        //    NatSessionManager.refreshAllSessionKey();
    }

    InetSocketAddress getDestAddress(SocketChannel localChannel) {
        short portKey = (short) localChannel.socket().getPort();
        NatSession session = NatSessionManager.getSession(portKey);
        if (session != null) {
            return new InetSocketAddress(localChannel.socket().getInetAddress(), session.remotePort & 0xFFFF);
        }
        return null;
    }

    void onAccepted(SelectionKey key) {
        ITcpTunnel localTunnel = null;
        try {

            SocketChannel localChannel = mServerSocketChannel.accept();
            short portKey = (short) localChannel.socket().getPort();
            localTunnel = TunnelFactory.wrapLocal(localChannel, mSelector, portKey);
            InetSocketAddress destAddress = getDestAddress(localChannel);
            if (destAddress != null) {

                ITcpTunnel remoteTunnel = TunnelFactory.wrapRemote(SocketChannel.open(), mSelector, portKey);
                //关联兄弟
                remoteTunnel.setBrotherTunnel(localTunnel);
                localTunnel.setBrotherTunnel(remoteTunnel);
                //开始连接
                remoteTunnel.connect(destAddress);
            }
        } catch (Exception ex) {
            VPNLog.e(TAG, "TcpProxyServer onAccepted catch an exception: failed " + ex.getMessage());
            if (DEBUG_LOG) {
                ex.printStackTrace();
            }
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }

}
