package com.minhui.networkcapture.ui;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.minhui.networkcapture.MainCaptureActivity;
import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseFragment;
import com.minhui.networkcapture.utils.AppConstants;
import com.minhui.networkcapture.utils.PermissionUtil;
import com.minhui.vpn.utils.MyFileUtils;
import com.minhui.vpn.service.CaptureVpnService;
import com.minhui.vpn.utils.ThreadProxy;


import java.io.File;
import java.io.FileFilter;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.minhui.vpn.utils.VPNDirConstants.BASE_DIR;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/5.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class SettingFragment extends BaseFragment {


    private static final String TAG = "SettingFragment";
    private static final int REQUEST_TO_EXPORT_CERT = 1005;

    private Handler handler;
    private ProgressBar pb;
    private SharedPreferences sp;
    private TextView cacheSizeTv;
    private long lastTouchTime = 0;
    private int touchNum = 0;
    private long ONE_SECOND = 1000;
    private boolean isDevMode;
    private View emailView;
    private boolean hasBeenDestroyed;

    @Override
    protected int getLayout() {
        return R.layout.fragment_setting;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.clear_cache_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDeletingCache) {
                    return;
                }
                isDeletingCache = true;
                pb.setVisibility(View.VISIBLE);
                clearHistoryData();
            }
        });

        view.findViewById(R.id.about_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
            }
        });

        pb = view.findViewById(R.id.pb);
        cacheSizeTv = view.findViewById(R.id.cache_size);
        String saveCacheSize = sp.getString(AppConstants.CACHE_SIZE, "");
        cacheSizeTv.setText(saveCacheSize);
        handler = new Handler(Looper.getMainLooper());

        isDevMode = sp.getBoolean(AppConstants.IS_DE_MODE, false);
        emailView = view.findViewById(R.id.email_setting);
        if (!isDevMode) {
            emailView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() != MotionEvent.ACTION_UP) {
                        return false;
                    }
                    long touchTime = System.currentTimeMillis();
                    if (touchTime - lastTouchTime > ONE_SECOND) {
                        touchNum = 0;
                    }
                    lastTouchTime = touchTime;
                    touchNum++;
                    if (touchNum >= 5) {
                        sp.edit().putBoolean(AppConstants.IS_DE_MODE, true).apply();
                        isDevMode = true;
                    }
                    return false;
                }
            });
        }
        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDevMode) {
                    startActivity(new Intent(getActivity(), LogCollectorActivity.class));
                }
            }
        });
        refreshCacheSize();
        hasBeenDestroyed = false;


        view.findViewById(R.id.privacy_policy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebShowActivity.Companion.startPrivacyPolicy(getActivity());
            }
        });
        view.findViewById(R.id.use_license).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebShowActivity.Companion.startUsePolicy(getActivity());
            }
        });

    }





    @Override
    public void onStart() {
        super.onStart();
        // initAds();

    }


    private void refreshCacheSize() {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                double cacheSizeDouble = MyFileUtils.getFileOrFilesSize(BASE_DIR, MyFileUtils.SIZETYPE_MB);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String cacheSizeStr = String.valueOf(cacheSizeDouble) + "mb";
                        sp.edit().putString(AppConstants.CACHE_SIZE, cacheSizeStr).apply();
                        if (!hasBeenDestroyed) {
                            cacheSizeTv.setText(cacheSizeStr);
                        }
                    }
                });


            }
        });


    }


    private boolean isDeletingCache;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hasBeenDestroyed = true;
    }

    private void clearHistoryData() {
        ThreadProxy.getInstance().execute(new Runnable() {


            @Override
            public void run() {

                File file = new File(BASE_DIR);
                MyFileUtils.deleteFile(file, new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (!pathname.exists()) {
                            return false;
                        }

                        String lastVpnStartTimeStr = CaptureVpnService.lastVpnStartTimeFormat;
                        if (lastVpnStartTimeStr == null) {
                            return true;
                        }
                        String absolutePath = pathname.getAbsolutePath();
                        //如果所选择文件是最近一次产生的，则不删除
                        return !absolutePath.contains(lastVpnStartTimeStr);
                    }
                });
                isDeletingCache = false;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        sp.edit().putString(AppConstants.CACHE_SIZE, "").apply();
                        if (!hasBeenDestroyed) {
                            pb.setVisibility(View.GONE);
                            showMessage(getString(R.string.success_clear_history_data));
                        }
                    }
                });
                refreshCacheSize();

            }
        });


    }

    private void showMessage(String string) {
        Toast.makeText(getActivity(), string, Toast.LENGTH_SHORT).show();
    }
}
