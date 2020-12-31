package com.minhui.vpn.log;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.annotation.Keep;

import com.minhui.vpn.log.VLog;
import com.minhui.vpn.utils.TimeFormatUtil;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by minhui.zhu on 2017/10/26.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */
@Keep
public class VPNLog {
    private static final String TAG = "VPNLog";

    private static VLog sVLogs;

    public static boolean debug=false;
    private VPNLog() {
    }

    /**
     * Switch the debug state.
     *
     * @param enable      If true to open the logcat.
     * @param debugLogDir The dir to save log files.
     */
    public static void setDebug(boolean enable, File debugLogDir) {
        debug=enable;
        /* clearOverTowDayFile(debugLogDir);*/
        File logFile = getFileName(debugLogDir);
        VLog.VLogConfig config = new VLog.Builder()
                .tag(TAG)
                .enable(enable)
                .consoleConfig(new VLog.ConsoleConfig(true))
                .fileConfig(new VLog.FileConfig(logFile))
                .build();
        sVLogs = VLog.get(config);
    }
    private static File getFileName(File debugLogDir) {
        if (debugLogDir != null) {
            long time = System.currentTimeMillis();
            String date = (String) DateFormat.format("yyyy-MM-dd",
                    time);
            String strTime = (String) DateFormat.format("HH:mm:ss",
                    time);
            String fileName = strTime + ".log";
            File file = new File(debugLogDir, date);
            if(!file.exists()){
                file.mkdirs();
            }
            return new File(file,fileName);
        }
        return null;
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param msg The message you would like logged.
     */
    public static void v(String msg) {
        v(TAG, msg);
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param msg The message you would like logged.
     */
    public static void d(String msg) {
        d(TAG, msg);
    }

    /**
     * Send a {@link Log#INFO} log message.
     *
     * @param msg The message you would like logged.
     */
    public static void i(String msg) {
        i(TAG, msg);
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param msg The message you would like logged.
     */
    public static void w(String msg) {
        w(TAG, msg);
    }

    /**
     * Send a {@link Log#ERROR} log message.
     *
     * @param msg The message you would like logged.
     */
    public static void e(String msg) {
        e(TAG, msg);
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param throwable An exception to log.
     */
    public static void wtf(Throwable throwable) {
        wtf(TAG, throwable);
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void v(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        if (sVLogs != null) {
            sVLogs.v(tag, msg);
        }
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        if (sVLogs != null) {
            sVLogs.d(tag, msg);
        }
    }


    /**
     * Send a {@link Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void i(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        if (sVLogs != null) {
            sVLogs.i(tag, msg);
        }
    }

    /**
     * Send a {@link Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void w(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        if (sVLogs != null) {
            sVLogs.w(tag, msg);
        }
    }

    /**
     * Send a {@link Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void e(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        if (sVLogs != null) {
            sVLogs.e(tag, msg);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     *
     * @param tag       Used to identify the source of a log message.
     * @param throwable An exception to log.
     */
    public static void wtf(String tag, Throwable throwable) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty!");
        }
        if (sVLogs != null) {
            sVLogs.wtf(tag, throwable);
        }
    }
   public static File getBaseDir(Context context){
        return  new File(context.getExternalCacheDir( ),"Logs/") ;
   }
    public static void initLog(Context context) {
        File file =new File(getBaseDir(context),
                TimeFormatUtil.formatYYMMDDHHMMSS(System.currentTimeMillis()));
        VPNLog.setDebug(true, file);
    }
}
