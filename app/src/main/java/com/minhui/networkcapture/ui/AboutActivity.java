package com.minhui.networkcapture.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.minhui.networkcapture.BuildConfig;
import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseActivity;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/5/6.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((TextView)findViewById(R.id.version)).setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_about;
    }
}
