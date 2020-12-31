package com.minhui.networkcapture;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.minhui.networkcapture.utils.AppConstants;
import com.minhui.networkcapture.utils.ContextUtil;
import com.minhui.vpn.VpnServiceHelper;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.utils.ThreadProxy;
import com.minhui.vpn.utils.TimeFormatUtil;
import com.minhui.vpn.utils.VPNDirConstants;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;


/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/4/30.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class MyApplication extends MultiDexApplication {
    public static final String BUGLY_ID = "2496a89406";
    public static final String BUGLY_ID_PRO = "25379f0ccc";
    private static final String TAG = "MyApplication";
    private static final CharSequence MY_PG_NAME = "minhui";
    private static Context context;
    private SharedPreferences sp;
    private Handler mHandler;
    private static final int WAIT_TO_START_KEEP = 60 * 1000;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mHandler = new Handler();
        sp = getSharedPreferences(AppConstants.DATA_SAVE, MODE_PRIVATE);
        if (ContextUtil.isProVersion(getContext())) {
            CrashReport.initCrashReport(getApplicationContext(), BUGLY_ID_PRO, false);
        } else {
            CrashReport.initCrashReport(getApplicationContext(), BUGLY_ID, false);
        }
        initVPNDir();
        initLog();
        VPNLog.initLog(getContext());
        //initCertificate();
        //initKeepAlive();
       // saveFirstOpenTime();
        VpnServiceHelper.initContext(getContext());
    }

    private void saveFirstOpenTime() {
        long firstOpenTime = sp.getLong(AppConstants.FIRST_OPEN_TIME, 0);
        if (firstOpenTime == 0 || firstOpenTime > System.currentTimeMillis()) {
            firstOpenTime = System.currentTimeMillis();
            sp.edit().putLong(AppConstants.FIRST_OPEN_TIME, firstOpenTime).apply();
        }
    }

    private void initLog() {
        try {
            boolean enableLog = context.getSharedPreferences(AppConstants.DATA_SAVE, MODE_PRIVATE)
                    .getBoolean(AppConstants.ENABLE_LOG, false);
            if (BuildConfig.DEBUG || enableLog) {
                VPNLog.initLog(getContext());
            }
        } catch (Exception e) {
            Log.d(TAG, "failed to init log");
        }
    }

    private void initVPNDir() {
        VPNDirConstants.setBaseDirName(this, getString(R.string.base_dir_name));
    }

    public static Context getContext() {
        return context.getApplicationContext();
    }

}
