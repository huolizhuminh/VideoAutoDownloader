package com.minhui.vpn.parser;

import okio.BufferedSource;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/28.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public interface SourceIntercept {
    BufferedSource intercept(BufferedSource bufferedSource);
}
