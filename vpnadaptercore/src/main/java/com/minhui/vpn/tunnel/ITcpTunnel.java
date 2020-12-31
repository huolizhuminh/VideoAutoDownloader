package com.minhui.vpn.tunnel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/16.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public interface ITcpTunnel extends KeyHandler, DataHandler {
    long NEED_CLEAN_SESSION_TIME = 500;

    /**
     * 断开连接，并让关联Tunnel断连
     */
    void dispose();

    /**
     * 断开连接，可控制关联Tunnel是否断连
     *
     * @param disposeBrother 控制关联Tunnel是否断连
     */

    void disposeInternal(boolean disposeBrother);

    /**
     * 增加需要发送的数据包
     *
     * @param buffer 需要发送的数据包
     */

    void addWriteData(ByteBuffer buffer);


    /**
     * 设置关联Tunnel
     *
     * @param tunnel 关联Tunnel
     */
    void setBrotherTunnel(ITcpTunnel tunnel);

    /**
     * 进行连接
     *
     * @param destAddress 需要连接服务端的地址
     */
    void connect(InetSocketAddress destAddress) throws Exception;

    boolean hasDepose();

    void refreshKeyState();


    boolean hasWriteCache();

    void setDisposeAfterFinishWrite();

    boolean needCleanSession();
}
