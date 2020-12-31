package com.minhui.vpn.log;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public final class VLog implements ILog {

    /**
     * Priority constant for the println method; use Log.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use Log.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use Log.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use Log.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use Log.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method; use Log.wtf.
     */
    public static final int WTF = 7;

    private ILog mInternalLog;
    private String mTag;

    private VLog(String tag, ILog internalLog) {
        this.mTag = tag;
        this.mInternalLog = internalLog;
    }

    /**
     * Get a {@link VLog} instance by config.
     *
     * @param config
     * @return
     */
    public static VLog get(VLogConfig config) {
        return new VLog(config.tag, LogFactory.create(config));
    }

    /**
     * Set a default tag.
     *
     * @param tag Used to identify the source of a log message.
     */
    public void setTag(String tag) {
        mTag = tag;
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param msg The message you would like logged.
     */
    public void v(String msg) {
        v(mTag, msg);
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param msg The message you would like logged.
     */
    public void d(String msg) {
        v(mTag, msg);
    }

    /**
     * Send a {@link Log#INFO} log message.
     *
     * @param msg The message you would like logged.
     */
    public void i(String msg) {
        i(mTag, msg);
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param msg The message you would like logged.
     */
    public void w(String msg) {
        v(mTag, msg);
    }

    /**
     * Send a {@link Log#ERROR} log message.
     *
     * @param msg The message you would like logged.
     */
    public void e(String msg) {
        e(mTag, msg);
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param throwable An exception to log.
     */
    public void wtf(Throwable throwable) {
        wtf(mTag, throwable);
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    @Override
    public void v(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        mInternalLog.v(tag, msg);
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    @Override
    public void d(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        mInternalLog.d(tag, msg);
    }


    /**
     * Send a {@link Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    @Override
    public void i(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        mInternalLog.i(tag, msg);
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    @Override
    public void w(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        mInternalLog.w(tag, msg);
    }

    /**
     * Send a {@link Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    @Override
    public void e(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        mInternalLog.e(tag, msg);
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     *
     * @param tag Used to identify the source of a log message.
     * @param throwable An exception to log.
     */
    @Override
    public void wtf(String tag, Throwable throwable) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        mInternalLog.wtf(tag, throwable);
    }

    @Override
    public void close() throws IOException {
        mInternalLog.close();
    }

    public static class VLogConfig {

        String tag;
        boolean enable;
        ConsoleConfig consoleConfig;
        FileConfig fileConfig;

    }

    public static class ConsoleConfig {

        public boolean output;

        public ConsoleConfig(boolean output) {
            this.output = output;
        }

    }

    public static class FileConfig {

        public final File logFile;

        // default empty means log all levels to file
        public Integer[] levelsToFile = new Integer[]{};

        // default empty means log all tags to file
        public String[] tagsToFile = new String[]{};

        // default 2M
        public long limitSize = 2 * 1024 * 1024;

        // file headers
        public String[] headers;

        public FileConfig(File logFile) {
            this.logFile = logFile;
        }

        /**
         * Only {@link #VERBOSE} and {@link #DEBUG} log to file.
         */
        public static Integer[] LEVELS_AS_DEBUG = new Integer[] {VERBOSE, DEBUG};

        /**
         * Only {@link #INFO}, {@link #WARN}, {@link #ERROR}, {@link #WTF} log to file.
         */
        public static Integer[] LEVELS_AS_RELEASE = new Integer[] {INFO, WARN, ERROR, WTF};

        /**
         * All levels log to file.
         */
        public static Integer[] LEVELS_AS_ALL = new Integer[] {VERBOSE, DEBUG, INFO, WARN, ERROR, WTF};

    }

    public static class Builder {

        VLogConfig config;

        public Builder() {
            config = new VLogConfig();
        }

        public Builder tag(String tag) {
            config.tag = tag;
            return this;
        }

        public Builder enable(boolean enable) {
            config.enable = enable;
            return this;
        }

        public Builder fileConfig(FileConfig fileConfig) {
            config.fileConfig = fileConfig;
            return this;
        }

        public Builder consoleConfig(ConsoleConfig consoleConfig) {
            config.consoleConfig = consoleConfig;
            return this;
        }

        public VLogConfig build() {
            return config;
        }

    }

}
