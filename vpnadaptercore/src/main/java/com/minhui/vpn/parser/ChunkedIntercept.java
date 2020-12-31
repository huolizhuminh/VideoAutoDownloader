package com.minhui.vpn.parser;

import okio.BufferedSource;
import okio.Okio;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/28.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class ChunkedIntercept implements SourceIntercept {

    @Override
    public BufferedSource intercept(BufferedSource buffer) {
        return Okio.buffer(new ChunkSource(buffer));
    }
}
