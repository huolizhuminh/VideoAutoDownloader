package com.minhui.vpn.log;

import android.util.Log;

import java.io.IOException;

/**
 * {@link Log} internal, print log to android logcat console.
 *
 * @author minhui
 * @since 17/9/22 21:24
 */

/* package */ final class AndroidLog implements ILog {

    @Override
    public void v(String tag, String msg) {
        Log.v(tag, msg);
    }

    @Override
    public void d(String tag, String msg) {
        Log.d(tag, msg);
    }

    @Override
    public void i(String tag, String msg) {
        Log.i(tag, msg);
    }

    @Override
    public void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    @Override
    public void w(String tag, String msg) {
        Log.w(tag, msg);
    }

    @Override
    public void wtf(String tag, Throwable throwable) {
        Log.wtf(tag, throwable);
    }

    @Override
    public void close() throws IOException {
    }

}
