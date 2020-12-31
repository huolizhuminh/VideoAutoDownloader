package com.minhui.vpn.parser;

import java.io.IOException;

import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingTimeout;
import okio.Source;
import okio.Timeout;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/16.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public abstract class AbstractSource implements Source {
    protected boolean closed;
    protected long bytesRead = 0;
    BufferedSource source;
    protected final ForwardingTimeout timeout;

    public AbstractSource(BufferedSource source) {
        this.source = source;
        timeout = new ForwardingTimeout(source.timeout());
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        try {
            long read = source.read(sink, byteCount);
            if (read > 0) {
                bytesRead += read;
            }
            return read;
        } catch (IOException e) {
            detachTimeout(timeout);
            throw e;
        }

    }

    @Override
    public Timeout timeout() {
        return timeout;
    }

    protected void detachTimeout(ForwardingTimeout timeout) {
        Timeout oldDelegate = timeout.delegate();
        timeout.setDelegate(Timeout.NONE);
        oldDelegate.clearDeadline();
        oldDelegate.clearTimeout();
    }
}






























