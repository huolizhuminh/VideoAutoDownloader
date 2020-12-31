package com.minhui.networkcapture.ads.banner;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.minhui.networkcapture.BuildConfig;
import com.minhui.networkcapture.ads.AdsConstant;
import com.minhui.networkcapture.utils.ContextUtil;
import com.minhui.vpn.log.VPNLog;


/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/12.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class MyAdsView extends FrameLayout {
    private static final String TAG = "AdsView";
    private AdView adViewInner;

    public MyAdsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setVisibility(GONE);
    }

    public void initAds(String id) {

        initAds(id, null);
    }

    public void initAds(String id, AdListener listener) {
        if(ContextUtil.isProVersion(getContext())){
            return;
        }
        if(ContextUtil.hasRegister(getContext())){
            return;
        }
        adViewInner = new AdView(getContext());
        if (BuildConfig.DEBUG) {
            Log.d(TAG,"initAds adsView = "+ adViewInner);
            adViewInner.setAdUnitId(AdsConstant.DEBUG_BANNER_ID);
        } else {
            Log.d(TAG,"initAds adsView = "+ adViewInner);
            adViewInner.setAdUnitId(id);
        }
        adViewInner.setAdSize(AdSize.SMART_BANNER);
        AdRequest adRequest = new AdRequest.Builder().build();
        try {
            adViewInner.loadAd(adRequest);
        } catch (Exception e) {
            VPNLog.e(TAG, "failed to loadAd");
        }


        addView(adViewInner);

        adViewInner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                VPNLog.i(TAG, "onAdLoaded");
                if (getVisibility() == GONE) {
                    setVisibility(View.VISIBLE);
                }

                if (listener != null) {
                    listener.onAdLoaded();
                }

            }

            @Override
            public void onAdFailedToLoad(int i) {
                VPNLog.i(TAG, "onAdFailedToLoad "+i);
            }
        });
        VPNLog.i(TAG, "end add ads");
    }

    public void removeAds() {
        removeView(adViewInner);
    }
}
