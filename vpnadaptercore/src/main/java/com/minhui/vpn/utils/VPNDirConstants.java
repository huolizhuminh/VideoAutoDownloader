package com.minhui.vpn.utils;

import android.content.Context;
import android.os.Environment;

public class VPNDirConstants {
    public static final String VIDEO_PATH = "videoCapture/";
    private static final String RAW_VIDEO = "rawVideo/";
    public static String ALL_PARSE_RAW_PATH;
    public static String BASE_DIR;
    public static String BASE_RAW_DIR;
    public static String BASE_PARSE;
    public static String DATA_DIR;
    public static String BASE;
    public static String ALL_PARSE_VIDEO_PATH;

    public static void setBaseDirName(Context context, String basePath) {
        BASE = basePath;
        BASE_DIR = context.getExternalCacheDir() + "/" + basePath + "/";
        BASE_RAW_DIR = BASE_DIR + "Conversation/";
        BASE_PARSE = BASE_DIR + "ParseData/";
        DATA_DIR = BASE_RAW_DIR + "data/";
        ALL_PARSE_VIDEO_PATH = BASE_PARSE + VPNDirConstants.VIDEO_PATH;
        ALL_PARSE_RAW_PATH = BASE_PARSE + VPNDirConstants.RAW_VIDEO;
    }
}
