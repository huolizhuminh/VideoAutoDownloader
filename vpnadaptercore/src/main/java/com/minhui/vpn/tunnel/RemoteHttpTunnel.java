package com.minhui.vpn.tunnel;

import android.os.Handler;
import android.os.Looper;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/17.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class RemoteHttpTunnel extends BaseHttpTunnel {
    protected String TAG = "RemoteHttpTunnel";
    private final Handler handler;
    private final DataHandler dataHandlerDelegate;

    public RemoteHttpTunnel(Selector selector, SocketChannel mInnerChannel, short portKey) {
        super(selector, mInnerChannel, portKey);
        handler = new Handler(Looper.getMainLooper());
        session.setRemoteTunnel(this);
        dataHandlerDelegate = DataHandlerFactory.createHandler(session);
    }
    @Override
    protected String initTAG() {
        return TAG = "Re:" + ((int) session.getLocalPort() & 0XFFFF);
    }
    @Override
    public void afterReceived(ByteBuffer buffer) {
        dataHandlerDelegate.afterReceived(buffer);
    }

    @Override
    public void beforeSend(ByteBuffer buffer) {
       dataHandlerDelegate.beforeSend(buffer);
    }

    @Override
    public void onDispose() {
       dataHandlerDelegate.onDispose();
    }

}
