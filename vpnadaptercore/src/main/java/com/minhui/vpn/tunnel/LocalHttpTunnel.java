package com.minhui.vpn.tunnel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/23.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class LocalHttpTunnel extends BaseHttpTunnel {
    public LocalHttpTunnel(Selector selector, SocketChannel mInnerChannel, short portKey) {
        super(selector, mInnerChannel, portKey);
        session.setLocalTunnel(this);
        hasConnected=true;
        try {
            mInnerChannel.configureBlocking(false);
            selector.wakeup();
            mInnerChannel.register(selector, SelectionKey.OP_READ,this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected String initTAG() {
        return TAG = "Lo:" + ((int) session.getLocalPort() & 0XFFFF);
    }
    @Override
    public void afterReceived(ByteBuffer buffer) {

        super.afterReceived(buffer);
    }
}
