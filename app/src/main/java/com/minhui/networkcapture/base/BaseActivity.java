package com.minhui.networkcapture.base;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.minhui.networkcapture.R;
import com.minhui.networkcapture.utils.AppConstants;
import com.minhui.networkcapture.utils.ContextUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/9.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private Unbinder bind;
    protected Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setContentView(getLayout());
        bind = ButterKnife.bind(this);
        enableBackIndicator();
    }

    protected abstract int getLayout();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bind.unbind();

    }

    @Override
    public void setTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void setTitle(int titleId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(titleId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!isMainActivity()){
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected boolean isMainActivity() {
        return false;
    }

    public void disableBackIndicator() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    public void enableBackIndicator() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setBackIndicator(int resId) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(resId);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setBackIndicator(Drawable drawable) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(drawable);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void showMsg(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    public void recommendPro(String message) {
        if(!ContextUtil.isGooglePlayChannel(getApplicationContext())
                ||ContextUtil.isProVersion(getApplicationContext())){
            return;
        }
        AlertDialog show = new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gotoPro();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getString(R.string.not_now), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        show.setCanceledOnTouchOutside(false);
    }
    public void gotoPro() {
        if(!ContextUtil.isGooglePlayChannel(getApplicationContext())){
            launchBrowser("https://www.pgyer.com/wOLk");
            return;
        }
        final String appPackageName = "com.minhui.networkcapture.pro";
        try {
            Intent appStoreIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName));
            appStoreIntent.setPackage("com.android.vending");
            startActivity(appStoreIntent);
        } catch (Exception e) {
            try {
                String url = "https://play.google.com/store/apps/details?id=" + appPackageName;
                launchBrowser(url);
            } catch (Exception se) {
            }
        }
        getSharedPreferences(AppConstants.DATA_SAVE, MODE_PRIVATE).edit()
                .putBoolean(AppConstants.HAS_SHOW_RECOMMEND, true).apply();
    }
    public void launchBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.d(TAG, "failed to launchBrowser " + e.getMessage());
        }
    }
}
