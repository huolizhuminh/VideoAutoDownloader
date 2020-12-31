package com.minhui.networkcapture.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.Nullable;

import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseActivity;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.utils.VPNDirConstants;


public class HistoryItemActivity extends BaseActivity {
    private static final String HISTORY_VIDEO_PATH = "VIDEO_PATH";
    private static final String LOG_TAG = "HistoryItemActivity";
    private String mHistoryVideoPath;
    private VideoCaptureView videoCaptureView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mHistoryVideoPath = VPNDirConstants.BASE_PARSE + intent.getStringExtra(HISTORY_VIDEO_PATH)
                + "/" + VPNDirConstants.VIDEO_PATH;
        VPNLog.d(LOG_TAG, "onCreate path = " + mHistoryVideoPath);
        videoCaptureView = findViewById(R.id.video_capture_view);
    }

    public static void openActivity(Context context, String path) {
        Intent intent = new Intent(context, HistoryItemActivity.class);
        intent.putExtra(HISTORY_VIDEO_PATH, path);
        context.startActivity(intent);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_history_item;
    }

}
