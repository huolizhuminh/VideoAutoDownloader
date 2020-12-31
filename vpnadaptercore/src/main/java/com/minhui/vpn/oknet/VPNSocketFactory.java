package com.minhui.vpn.oknet;

import com.minhui.vpn.VpnServiceHelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

/**
 * Created by minhui.zhu on 2017/7/20.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class VPNSocketFactory extends SocketFactory {
    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {

        return new VPNProtectedSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {

        return new VPNProtectedSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return new VPNProtectedSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {

        return new VPNProtectedSocket(address, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket() throws IOException {
        return new VPNProtectedSocket();
    }

    public static class VPNProtectedSocket extends Socket {

        private static final String TAG = VPNProtectedSocket.class.getSimpleName();


        public VPNProtectedSocket(String host, int port) throws IOException {
            super(host, port);
        }

        VPNProtectedSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            super(host, port, localHost, localPort);
        }

        VPNProtectedSocket(InetAddress host, int port) throws IOException {
            super(host, port);
        }

        VPNProtectedSocket() {
            super();
        }

        VPNProtectedSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            super(address, port, localAddress, localPort);
        }


        @Override
        public void connect(SocketAddress endpoint, int timeout) throws IOException {
            VpnServiceHelper.protect(this);
            super.connect(endpoint, timeout);
        }
    }
}

