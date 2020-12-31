package com.minhui.networkcapture.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseActivity;
import com.minhui.networkcapture.utils.AppConstants;
import com.minhui.vpn.utils.ACache;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/19.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class CaptureFilterActivity extends BaseActivity {
    @BindView(R.id.capture_app)
    TextView captureApp;
    @BindView(R.id.select_app_container)
    RelativeLayout selectAppContainer;
    @BindView(R.id.capture_ip)
    TextView captureIp;
    @BindView(R.id.select_ip_container)
    RelativeLayout selectIpContainer;
    @BindView(R.id.capture_host)
    TextView captureHost;
    @BindView(R.id.select_host_container)
    RelativeLayout selectHostContainer;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();

    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshUI();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshUI();
            }
        },300);
    }

    private void refreshUI() {
        PackageShowInfo showInfo = (PackageShowInfo) ACache.get(getApplicationContext()).getAsObject(AppConstants.SELECT_PACKAGE);
        String captureAppStr;
        if(showInfo!=null){
            captureAppStr=getString(R.string.selected_app)+":"+(showInfo.appName==null?showInfo.packageName:showInfo.appName);
        }else {
            captureAppStr=getString(R.string.selected_app)+":"+getString(R.string.all);
        }
        //防止已经销毁从而产生奔溃
        if(isDestroyed()||isFinishing()){
            return;
        }
        captureApp.setText(captureAppStr);
        ArrayList<String> selectIPs = (ArrayList<String>) ACache.get(getApplicationContext()).getAsObject(AppConstants.SELECT_IP);
        captureIp.setText(getString(R.string.selected_ip)+getStringFromArray(selectIPs)) ;
        ArrayList<String> selectHosts = (ArrayList<String>) ACache.get(getApplicationContext()).getAsObject(AppConstants.SELECT_HOST);
        captureHost.setText(getString(R.string.selected_host)+getStringFromArray(selectHosts));
    }

    private String getStringFromArray(ArrayList<String> arrayList) {
        if(arrayList==null||arrayList.isEmpty()){
            return getString(R.string.not_select_yet);
        }else {
            StringBuilder builder = new StringBuilder();
            for(String item:arrayList){
                builder.append(item).append(" ");
            }
            return builder.toString();
        }
    }

    @OnClick(R.id.select_app_container)
    public void toSelectApp() {
        startActivity(new Intent(CaptureFilterActivity.this, PackageListActivity.class));

    }

    @OnClick(R.id.select_ip_container)
    public void toSelectIP() {
        startActivity(new Intent(CaptureFilterActivity.this, SelectIpActivity.class));

    }

    @OnClick(R.id.select_host_container)
    public void toSelectHost() {
        startActivity(new Intent(CaptureFilterActivity.this, SelectHostActivity.class));

    }

    @Override
    protected int getLayout() {
        return R.layout.activity_capture_filter;
    }
}
