package com.minhui.vpn.log;

import java.io.IOException;

/**
 * Empty log, do nothing.
 *
 * @author minhui
 * @since 17/9/22 21:40
 */

/* package */ final class EmptyLog implements ILog {

    /* package */ EmptyLog() {
    }

    @Override
    public void v(String tag, String msg) {
    }

    @Override
    public void d(String tag, String msg) {
    }

    @Override
    public void i(String tag, String msg) {
    }

    @Override
    public void e(String tag, String msg) {
    }

    @Override
    public void w(String tag, String msg) {
    }

    @Override
    public void wtf(String tag, Throwable throwable) {
    }

    @Override
    public void close() throws IOException {
    }

}
