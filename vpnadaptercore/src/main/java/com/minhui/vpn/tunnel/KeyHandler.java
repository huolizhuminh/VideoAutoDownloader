package com.minhui.vpn.tunnel;

import java.nio.channels.SelectionKey;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/11.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public interface KeyHandler {
    void onKeyReady(SelectionKey key) /*throws Exception*/;
}
