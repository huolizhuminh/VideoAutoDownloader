package com.minhui.vpn.log;

import android.util.Log;

import java.io.Closeable;

/**
 * API for sending log output.
 *
 * @author minhui
 * @since 17/9/22 21:25
 */

/* package */ interface ILog extends Closeable {

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    void v(String tag, String msg);

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    void d(String tag, String msg);

    /**
     * Send a {@link Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    void i(String tag, String msg);

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    void w(String tag, String msg);

    /**
     * Send a {@link Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    void e(String tag, String msg);

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     *
     * @param tag Used to identify the source of a log message.
     * @param throwable An exception to log.
     */
    void wtf(String tag, Throwable throwable);

}
