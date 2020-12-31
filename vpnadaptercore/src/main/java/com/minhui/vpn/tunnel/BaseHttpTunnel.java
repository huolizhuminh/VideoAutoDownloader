package com.minhui.vpn.tunnel;

import android.os.Handler;
import android.os.Looper;

import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.nat.NatSessionManager;
import com.minhui.vpn.service.CaptureVpnService;
import com.minhui.vpn.VpnServiceHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/16.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class BaseHttpTunnel implements ITcpTunnel {
    private static final boolean DEBUG_LOG = false;
    Selector mSelector;
    protected SocketChannel mInnerChannel;

    ITcpTunnel brotherTunnel = null;
    short portKey;
    NatSession session;
    ConcurrentLinkedQueue<ByteBuffer> needWriteData = new ConcurrentLinkedQueue<>();
    protected boolean mDisposed = false;
    protected boolean hasConnected = false;
    private final Handler handler;
    protected String TAG = null;
    private boolean logebug = false;
    private boolean mDisposeAfterFinishWrite;
    private ByteBuffer mSendRemainBuffer;
    private long mDisposeTime;

    public BaseHttpTunnel(Selector selector, SocketChannel mInnerChannel, short portKey) {
        this.mSelector = selector;
        this.mInnerChannel = mInnerChannel;
        this.portKey = portKey;
        session = NatSessionManager.getSession(portKey);
        TAG = initTAG();
        handler = new Handler(Looper.getMainLooper());
        log(TAG, "onCreate");
    }

    protected String initTAG() {
        return getClass().getSimpleName() + ":" + (session.getLocalPort() & 0XFFFF);
    }

    @Override
    public void connect(InetSocketAddress destAddress) throws Exception {
        log(TAG, "connect");
        //保护socket不走VPN
        if (VpnServiceHelper.protect(mInnerChannel.socket())) {
            //注册连接事件
            mSelector.wakeup();
            mInnerChannel.configureBlocking(false);
            mInnerChannel.register(mSelector, SelectionKey.OP_CONNECT, this);
            mInnerChannel.connect(destAddress);
            log(TAG, "Connecting to %s" + destAddress);
        } else {
            throw new Exception("VPN protect socket failed.");
        }
    }

    @Override
    public void onKeyReady(SelectionKey key) {
        if (key.isReadable()) {
            onReadable(key);
        } else if (key.isWritable()) {
            onWritable(key);
        } else if (key.isConnectable()) {
            onConnectable();
        }
    }

    @Override
    public void setBrotherTunnel(ITcpTunnel tunnel) {
        brotherTunnel = tunnel;
    }

    protected void onConnectable() {
        log(TAG, "onConnectable");
        try {
            if (mInnerChannel.finishConnect()) {
                //通知子类TCP已连接，子类可以根据协议实现握手等
                log(TAG, "onConnected");
                onConnected();
            } /*else {
                this.dispose();
            }*/
        } catch (Exception e) {
            log(TAG, "onConnectable error " + e.getMessage());
            dispose();
        }
    }

    protected void onConnected() {
        hasConnected = true;
        refreshKeyState(); //开始接收数据
    }

    @Override
    public void refreshKeyState() {
        if (!hasConnected) {
            return;
        }
        try {
            if (mInnerChannel.isBlocking()) {
                mInnerChannel.configureBlocking(false);
            }
            mSelector.wakeup();
            int ops;
            if (hasWriteCache()) {
                ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
            } else {
                ops = SelectionKey.OP_READ;
            }
            //注册读事件
            mInnerChannel.register(mSelector, ops, this);
        } catch (IOException e) {
            if (logebug) {
                e.printStackTrace(System.err);
            }
            dispose();
        }
    }

    @Override
    public boolean hasWriteCache() {
        if (needWriteData!=null&&needWriteData.size() > 0) {
            return true;
        }
        if (mSendRemainBuffer != null && mSendRemainBuffer.hasRemaining()) {
            return true;
        }
        return false;
    }

    @Override
    public void setDisposeAfterFinishWrite() {
        mDisposeAfterFinishWrite = true;
    }

    protected void onWritable(SelectionKey key) {
        log(TAG, "onWritable ");
        try {
            if (mSendRemainBuffer == null || !mSendRemainBuffer.hasRemaining()) {
                mSendRemainBuffer = needWriteData.poll();
            }
            if (mSendRemainBuffer == null || !mSendRemainBuffer.hasRemaining()) {
                refreshKeyState();
                return;
            }
            int writeData = write(mSendRemainBuffer);
            log(TAG, "end write write size " + writeData);
            refreshKeyState();
        } catch (Exception ex) {
            if (logebug) {
                ex.printStackTrace(System.err);
            }

            log(TAG, "onWritable catch an exception: %s" + ex.getMessage());

            this.dispose();
        }
    }

    protected int write(ByteBuffer buffer) throws Exception {
        log(TAG, "write ");
        int byteSendSum = 0;
        beforeSend(buffer);
        while (buffer.hasRemaining()) {
            int byteSent = mInnerChannel.write(buffer);
            byteSendSum += byteSent;
            if (byteSent == 0) {
                break; //不能再发送了，终止循环
            }
        }
        return byteSendSum;

    }

    private void log(String tag, String msg) {
        if (DEBUG_LOG) {
            VPNLog.d(TAG, msg);
        }
    }

    protected void onReadable(SelectionKey key) {
        log(TAG, "onReadable");
        try {
            ByteBuffer buffer = ByteBuffer.allocate(CaptureVpnService.MUTE_SIZE);
            buffer.clear();
            log(TAG, "begin to read ");
            int bytesRead = mInnerChannel.read(buffer);
            buffer.flip();
            log(TAG, "end  read size " + bytesRead);
            if (bytesRead > 0) {
                //先让子类处理，例如解密数据
                afterReceived(buffer);
                sendToBrother(buffer);
            } else if (bytesRead < 0) {
                this.dispose();
            }
        } catch (Exception ex) {
            if (logebug) {
                ex.printStackTrace(System.err);
            }
            log(TAG, "onReadable catch an exception: %s" + ex);
            this.dispose();
        }
    }

    protected void sendToBrother(ByteBuffer buffer) throws Exception {
        brotherTunnel.addWriteData(buffer);
    }

    @Override
    public void dispose() {
        log(TAG, "dispose");
        disposeInternal(true);
    }

    @Override
    public void addWriteData(ByteBuffer buffer) {
        needWriteData.offer(buffer);

        refreshKeyState();
    }

    @Override
    public void afterReceived(ByteBuffer buffer) {
        //do nothing
    }

    @Override
    public void beforeSend(ByteBuffer buffer) {
        //do nothing
    }
    @Override
    public boolean needCleanSession() {
        return mDisposed
                && (System.currentTimeMillis() - mDisposeTime > NEED_CLEAN_SESSION_TIME);
    }
    @Override
    public void disposeInternal(boolean disposeBrother) {
        log(TAG, "disposeInternal");
        if (mDisposed) {
            return;
        }
        mDisposeTime = System.currentTimeMillis();
        mDisposed = true;
        try {
            mInnerChannel.close();
        } catch (Exception ex) {
            if (logebug) {
                ex.printStackTrace(System.err);
            }
            log(TAG, "InnerChannel close catch an exception: %s" + ex);
        }

        if (brotherTunnel != null && disposeBrother) {
            if (brotherTunnel.hasWriteCache()) {
                brotherTunnel.setDisposeAfterFinishWrite();
            } else {
                brotherTunnel.disposeInternal(false);
                brotherTunnel = null;
            }
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    NatSessionManager.removeSession(portKey);
                }
            },NEED_CLEAN_SESSION_TIME);
        }

        mInnerChannel = null;
        mSelector = null;

        mDisposed = true;

        needWriteData = null;


        onDispose();


    }

    @Override
    public void onDispose() {

    }

    @Override
    public boolean hasDepose() {
        return mDisposed;
    }
}
