package com.minhui.vpn.log;

import java.io.IOException;
import java.util.List;

/**
 * A combination log contains some different log strategies.
 *
 * @author minhui
 * @since 17/9/23 09:42
 */

/* package */ class CombinationLog implements ILog {

    private final ILog[] mCombinationLogs;

    /* package */ CombinationLog(List<ILog> logs) {
        this.mCombinationLogs = new ILog[logs.size()];
        logs.toArray(mCombinationLogs);
    }

    /* package */ CombinationLog(ILog... logs) {
        this.mCombinationLogs = logs;
    }

    @Override
    public void v(String tag, String msg) {
        if (mCombinationLogs != null) {
            for (ILog log : mCombinationLogs) {
                log.v(tag, msg);
            }
        }
    }

    @Override
    public void d(String tag, String msg) {
        if (mCombinationLogs != null) {
            for (ILog log : mCombinationLogs) {
                log.d(tag, msg);
            }
        }
    }

    @Override
    public void i(String tag, String msg) {
        if (mCombinationLogs != null) {
            for (ILog log : mCombinationLogs) {
                log.i(tag, msg);
            }
        }
    }

    @Override
    public void w(String tag, String msg) {
        if (mCombinationLogs != null) {
            for (ILog log : mCombinationLogs) {
                log.w(tag, msg);
            }
        }
    }

    @Override
    public void e(String tag, String msg) {
        if (mCombinationLogs != null) {
            for (ILog log : mCombinationLogs) {
                log.e(tag, msg);
            }
        }
    }

    @Override
    public void wtf(String tag, Throwable throwable) {
        if (mCombinationLogs != null) {
            for (ILog log : mCombinationLogs) {
                log.wtf(tag, throwable);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (mCombinationLogs != null) {
            for (ILog log : mCombinationLogs) {
                log.close();
            }
        }
    }

}
