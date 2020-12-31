package com.minhui.networkcapture.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.minhui.networkcapture.BuildConfig;
import com.minhui.networkcapture.R;

import java.io.File;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;


/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/16.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class ContextUtil {
    private static final int MIN_TOUCH_SLOP_DP = 3;
    private static final String TAG = "ContextUtil";
    public static final String GOOGLE_PLAY = "googleplay";
    public static final String INSTALLED_CHANNEL = "InstallChannel";
    public static final String PUBLISH_VERSION = "PublishVersion";
    public static final String PROVIDER_NAME ="ProviderName";
    public static final String BUILD_TIME = "BuildVersionTime";
    private static final String PRO_VERSION = "pro";

    public static String getAppMetaData(Context ctx, String key) {
        if (ctx == null || TextUtils.isEmpty(key)) {
            return null;
        }
        String resultData = null;
        try {
            PackageManager packageManager = ctx.getPackageManager();
            if (packageManager != null) {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo != null) {
                    if (applicationInfo.metaData != null) {
                        Object value = applicationInfo.metaData.get(key);
                        Log.d(TAG,"getAppMetaData value = "+value+" key = "+key);
                        resultData = (String) value;
                    }
                }

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return resultData;
    }

    public static boolean isGooglePlayChannel(Context context) {
        String installChannel = ContextUtil.getAppMetaData(context, INSTALLED_CHANNEL);
        return GOOGLE_PLAY.equals(installChannel);
    }

    public static boolean isProVersion(Context context) {
        String installChannel = ContextUtil.getAppMetaData(context, PUBLISH_VERSION);
        return PRO_VERSION.equals(installChannel);
    }
    public static boolean hasRegister(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(AppConstants.DATA_SAVE, MODE_PRIVATE);
       return sharedPreferences.contains(AppConstants.REGISTER_KEY);
    }

    public static int dp2px(Context context, float dpValue) {
        final float densityScale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * densityScale + 0.5f);
    }

    public static float getMinTouchSlop(Context context) {
        return dp2px(context, MIN_TOUCH_SLOP_DP);
    }

    public static void launchBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.d(TAG, "failed to launchBrowser " + e.getMessage());
        }
    }

    public static String getLocal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else {
            return context.getResources().getConfiguration().locale.getLanguage();
        }
    }

    public static boolean isNoGooglePlayNoAds(Context context) {
        return false;
     /* if (isGooglePlayChannel(context)) {
            return false;
        }
        long buildTIme=0;
        try {
            buildTIme = getLong(getAppMetaData(context, BUILD_TIME).split("_")[1]);
        }catch (Exception e ){

        }
        if(buildTIme==0){
            return false;
        }
        long currentTIme = System.currentTimeMillis();
        long pushTime = currentTIme - buildTIme;
        if (pushTime <0||pushTime>72*3600*1000){
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(AppConstants.DATA_SAVE, MODE_PRIVATE);
        long firstOpenTime = sp.getLong(AppConstants.FIRST_OPEN_TIME, 0);
        long useTIme = currentTIme - firstOpenTime;
        if(useTIme >24*3600*1000){
            return false;
        }
        return true;*/
    }

    private static long getLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {

        }
        return 0;
    }

    public static void shareFile(Activity context,File file) {
        Uri videoUri = Uri.fromFile(file);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        Intent shareIntent = new Intent();

        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri);
        shareIntent.setType("text/*");
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_to)));
    }
}
