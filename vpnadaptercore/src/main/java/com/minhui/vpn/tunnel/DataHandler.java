package com.minhui.vpn.tunnel;

import java.nio.ByteBuffer;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/30.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public interface DataHandler {
    /**
     * 数据包发送之前触发，此数据包应该是加密之前的原始数据
     *
     * @param buffer 加密之前的原始数据
     */
    void beforeSend(ByteBuffer buffer);

    /**
     * 数据包读取之后触发，此数据包应该是完成解密后的数据
     *
     * @param buffer 解密后的数据
     */
    void afterReceived(ByteBuffer buffer);
    void onDispose();
}
