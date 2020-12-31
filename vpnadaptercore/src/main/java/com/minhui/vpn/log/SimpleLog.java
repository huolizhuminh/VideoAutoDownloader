package com.minhui.vpn.log;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A sample logger. The logger has some follow features.
 *
 * <ul>
 *   <li>Write to external cache dir, in /sdcard/Android/<package>/cache/log/xxx.log</li>
 *   <li>Print to android logcat console.</li>
 * </ul>
 *
 * @author minhui
 * @since 17/9/30 11:01
 */

public final class SimpleLog {

    private static final Map<String, SimpleLog> mSimpleLogs;

    private VLog sLog;

    static {
        mSimpleLogs = new ConcurrentHashMap<>();
    }

    /**
     * Get a simple log instance, and the same tag will share one instance.
     *
     * @param context Any context.
     * @param tag An log tag to use.
     * @return The instance shared by one tag.
     */
    public static SimpleLog get(Context context, String tag) {
        return mSimpleLogs.containsKey(tag) ? mSimpleLogs.get(tag) : new SimpleLog(context, tag);
    }

    private SimpleLog(Context context, String tag) {
        File logFile = new File(context.getExternalCacheDir(), "log/" + tag + "/" + DateFormat.format("yyyy-MM-dd HH-mm-ss",
                System.currentTimeMillis()) + ".log");
        VLog.VLogConfig config = new VLog.Builder()
                .tag(tag)
                .enable(true)
                .consoleConfig(new VLog.ConsoleConfig(true))
                .fileConfig(new VLog.FileConfig(logFile)).build();
        sLog = VLog.get(config);

        // add to cache
        mSimpleLogs.put(tag, this);
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param msg The message you would like logged.
     */
    public void v(String msg) {
        if (sLog != null) {
            sLog.v(msg);
        }
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param msg The message you would like logged.
     */
    public void d(String msg) {
        if (sLog != null) {
            sLog.d(msg);
        }
    }

    /**
     * Send a {@link Log#INFO} log message.
     *
     * @param msg The message you would like logged.
     */
    public void i(String msg) {
        if (sLog != null) {
            sLog.i(msg);
        }
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param msg The message you would like logged.
     */
    public void w(String msg) {
        if (sLog != null) {
            sLog.w(msg);
        }
    }

    /**
     * Send a {@link Log#ERROR} log message.
     *
     * @param msg The message you would like logged.
     */
    public void e(String msg) {
        if (sLog != null) {
            sLog.e(msg);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     *
     * @param throwable An exception to log.
     */
    public void wtf(Throwable throwable) {
        if (sLog != null) {
            sLog.wtf(throwable);
        }
    }

}
