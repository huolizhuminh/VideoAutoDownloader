package com.minhui.vpn.tunnel;

import android.net.VpnService;

import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.nat.NatSessionManager;
import com.minhui.vpn.udpip.Packet;
import com.minhui.vpn.utils.SocketUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by minhui.zhu on 2017/7/11.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class UDPTunnel implements KeyHandler {


    private static final boolean LOG_DEBUG = false;
    private String TAG;
    private final VpnService vpnService;
    private final Selector selector;
    private final UDPServer vpnServer;
    private final Queue<ByteBuffer> outputQueue;
    private Packet referencePacket;
    private SelectionKey selectionKey;

    private DatagramChannel channel;
    private final ConcurrentLinkedQueue<Packet> toNetWorkPackets = new ConcurrentLinkedQueue<>();
    public static final int HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
    private Short portKey;
    String ipAndPort;
    private final NatSession session;
    private final DataHandler networkDataHandlerDelegate;

    public UDPTunnel(VpnService vpnService, Selector selector, UDPServer vpnServer, Packet packet, Queue<ByteBuffer> outputQueue, short portKey) {
        this.vpnService = vpnService;
        this.selector = selector;
        this.vpnServer = vpnServer;
        this.referencePacket = packet;
        ipAndPort = packet.getIpAndPort();
        this.outputQueue = outputQueue;
        this.portKey = portKey;
        TAG = "udpTunnel" + ((int) (portKey & 0XFFFF));
        session = NatSessionManager.getSession(portKey);
        networkDataHandlerDelegate = DataHandlerFactory.createHandler(session);
    }


    private void processKey(SelectionKey key) {
        if (key.isWritable()) {
            processSend();
        } else if (key.isReadable()) {
            processReceived();
        }
        updateInterests();
    }

    private void log(String tag, String msg) {
        if (LOG_DEBUG) {
            VPNLog.d(TAG, msg);
        }
    }

    private void processReceived() {
        log(TAG, "processReceived:" + ipAndPort);
        ByteBuffer receiveBuffer = SocketUtils.getByteBuffer();
        // Leave space for the header
        receiveBuffer.position(HEADER_SIZE);
        int readBytes = 0;
        try {
            readBytes = channel.read(receiveBuffer);

        } catch (Exception e) {
            log(TAG, "failed to read udp datas ");
            vpnServer.closeUDPConn(this);
            return;
        }
        if (readBytes == -1) {
            vpnServer.closeUDPConn(this);
            log(TAG, "read  data error :" + ipAndPort);
        } else if (readBytes == 0) {
            log(TAG, "read no data :" + ipAndPort);
        } else {
            log(TAG, "read readBytes:" + readBytes + "ipAndPort:" + ipAndPort);
            Packet newPacket = referencePacket.duplicated();
            newPacket.updateUDPBuffer(receiveBuffer, readBytes);
            receiveBuffer.position(HEADER_SIZE + readBytes);
            receiveBuffer.flip();
            networkDataHandlerDelegate.afterReceived(receiveBuffer);
            outputQueue.offer(newPacket.backingBuffer);
            log(TAG, "read  data :readBytes:" + readBytes + "ipAndPort:" + ipAndPort);
            session.lastRefreshTime = System.currentTimeMillis();
        }
    }


    private void processSend() {
        log(TAG, "processWriteUDPData " + ipAndPort);
        Packet toNetWorkPacket = getToNetWorkPackets();
        if (toNetWorkPacket == null) {
            log(TAG, "write data  no packet ");
            return;
        }
        try {
            ByteBuffer payloadBuffer = toNetWorkPacket.backingBuffer;
            networkDataHandlerDelegate.beforeSend(payloadBuffer);
            session.lastRefreshTime = System.currentTimeMillis();
            while (payloadBuffer.hasRemaining()) {
                channel.write(payloadBuffer);
            }


        } catch (IOException e) {
            VPNLog.e(TAG, "Network write error: " + ipAndPort + "error is :" + e.getMessage());
            vpnServer.closeUDPConn(this);
        }
    }

    public void initConnection() {
        log(TAG, "init  ipAndPort:" + ipAndPort);
        InetAddress destinationAddress = referencePacket.ip4Header.destinationAddress;
        int destinationPort = referencePacket.udpHeader.destinationPort;
        try {
            channel = DatagramChannel.open();
            vpnService.protect(channel.socket());
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(destinationAddress, destinationPort));
            selector.wakeup();
            selectionKey = channel.register(selector,
                    SelectionKey.OP_READ, this);
        } catch (IOException e) {
            SocketUtils.closeResources(channel);
            return;
        }
        referencePacket.swapSourceAndDestination();
        addToNetWorkPacket(referencePacket);
    }

    public void processPacket(Packet packet) {
        addToNetWorkPacket(packet);
        updateInterests();
    }

    public void close() {
        log(TAG, "close");
        try {
            if (selectionKey != null) {
                selectionKey.cancel();
            }
            if (channel != null) {
                channel.close();
            }
            networkDataHandlerDelegate.onDispose();
        } catch (Exception e) {
            VPNLog.w(TAG, "error to close UDP channel IpAndPort" + ipAndPort + ",error is " + e.getMessage());
        }

    }


    Packet getToNetWorkPackets() {
        return toNetWorkPackets.poll();
    }

    void addToNetWorkPacket(Packet packet) {
        toNetWorkPackets.offer(packet);
        updateInterests();
    }

    DatagramChannel getChannel() {
        return channel;
    }

    void updateInterests() {
        int ops;
        if (toNetWorkPackets.isEmpty()) {
            ops = SelectionKey.OP_READ;
        } else {
            ops = SelectionKey.OP_WRITE | SelectionKey.OP_READ;
        }
        selector.wakeup();
        selectionKey.interestOps(ops);
        log(TAG, "updateInterests ops:" + ops + ",ip" + ipAndPort);
    }


    @Override
    public void onKeyReady(SelectionKey key) {
        processKey(key);
    }

    public Short getPortKey() {
        return portKey;
    }

}
