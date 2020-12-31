package com.minhui.networkcapture.ui;


import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseFragment;
import com.minhui.vpn.utils.ThreadProxy;
import com.minhui.vpn.utils.VPNDirConstants;

import java.io.File;
import java.util.ArrayList;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/5.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class HistoryFragment extends BaseFragment {

    private static final String LOG_TAG = "HistoryFragment";

    @Override
    protected int getLayout() {
        return R.layout.fragment_history;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        VideoCaptureView videoCaptureView = view.findViewById(R.id.videoCaptureView);
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                File file = new File(VPNDirConstants.ALL_PARSE_VIDEO_PATH);
                String[] list = file.list();
                ArrayList<String> fileList = new ArrayList<>();
                for(String fileName :list){
                    fileList.add(new File(file,fileName).getAbsolutePath());
                }
                videoCaptureView.refreshDataAndRefreshView(fileList);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
