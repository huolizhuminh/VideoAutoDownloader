package com.minhui.networkcapture.ads.interstitial;

import android.content.Context;
import android.os.Handler;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.minhui.networkcapture.BuildConfig;
import com.minhui.networkcapture.ads.AdsConstant;
import com.minhui.networkcapture.utils.ContextUtil;
import com.minhui.vpn.log.VPNLog;


/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/10.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class InterstitialManager {
    private static final String TAG = "InterstitialManager";
    private InterstitialAd mInterstitialAd;
    boolean hasLoaded = false;
    private Handler handler;
    private InterstitialListener listener;
    Context context;

    static class InnerClass {
        static InterstitialManager instance = new InterstitialManager();
    }

    private InterstitialManager() {

    }

    public static InterstitialManager getInstance() {
        return InnerClass.instance;
    }

    public boolean isHasLoaded() {
        return hasLoaded;
    }

    public void initContext(Context context) {
        this.context = context;
        mInterstitialAd = new InterstitialAd(context);
        if (BuildConfig.DEBUG) {
            mInterstitialAd.setAdUnitId(AdsConstant.DEBUG_FLASH_ID);
        } else {
            mInterstitialAd.setAdUnitId(AdsConstant.MAIN_FLASH);
        }
        loadAds();
        handler = new Handler();
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                hasLoaded = true;
                VPNLog.d(TAG, "onAdLoaded");

            }

            @Override
            public void onAdFailedToLoad(int i) {
                hasLoaded = false;
                VPNLog.d(TAG, "onAdFailedToLoad");
            }

            @Override
            public void onAdLeftApplication() {
                if (listener != null) {
                    listener.onLoadClosed();
                    listener = null;
                }

                VPNLog.d(TAG, "onAdLeftApplication");
                loadAds();
            }

            @Override
            public void onAdClosed() {
                loadAds();
                if (listener != null) {
                    listener.onLoadClosed();
                    listener = null;
                }
                VPNLog.d(TAG, "onAdClosed");
                loadAds();

            }
        });
    }

    private void loadAds() {
        VPNLog.d(TAG, "loadAds");
        if (BuildConfig.DEBUG) {
            mInterstitialAd.loadAd(new AdRequest
                    .Builder()
                    .addTestDevice("0AC4B0811C8DE87AB303B1B026F2FCEF")
                    .build());
        } else {
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
        }
    }

    public void showSplashAds(Context context) {
        if(ContextUtil.isProVersion(context)){
            return;
        }
        if(ContextUtil.hasRegister(context)){
            return;
        }
       /* if(ContextUtil.isGooglePlayChannel(context)){
            return;
        }*/
        if(ContextUtil.isNoGooglePlayNoAds(context)){
            return;
        }
        if (mInterstitialAd == null) {
            if (listener != null) {
                listener.onLoadClosed();
            }
            return;
        }
        if (!mInterstitialAd.isLoaded()) {
            if (listener != null) {
                listener.onLoadClosed();
            }
            return;
        }
        mInterstitialAd.show();
    }
}
