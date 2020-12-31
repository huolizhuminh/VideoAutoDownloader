package com.minhui.networkcapture.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.minhui.networkcapture.BuildConfig;
import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseActivity;
import com.minhui.networkcapture.utils.AppConstants;
import com.minhui.networkcapture.utils.ContextUtil;
import com.minhui.vpn.utils.MyFileUtils;
import com.minhui.networkcapture.view.CheckableImageView;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.utils.ThreadProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/9.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class LogCollectorActivity extends BaseActivity {
    private static final String LOG_TAG = "DevActivity";
    private SharedPreferences sp;
    private CheckableImageView enableLog;
    private RecyclerView listView;
    private View pg;
    private ArrayList<String> mFileName;
    private LogAdapter mAdapter;

    @Override
    protected int getLayout() {
        return R.layout.activity_dev;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(AppConstants.DATA_SAVE, Context.MODE_PRIVATE);
        enableLog = findViewById(R.id.enable_log);
        enableLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = enableLog.isChecked();
                enableLog.setChecked(!checked);
                sp.edit().putBoolean(AppConstants.ENABLE_LOG, !checked).apply();
                VPNLog.initLog(getApplicationContext());
            }
        });
        if(BuildConfig.DEBUG){
            findViewById(R.id.check_container).setVisibility(View.GONE);
        }
        boolean enableLogCurrent = sp.getBoolean(AppConstants.ENABLE_LOG, false);
        enableLog.setChecked(enableLogCurrent);
        listView = findViewById(R.id.list_view);
        listView.setLayoutManager(new WrapContentLinearLayoutManager(LogCollectorActivity.this));
        pg = findViewById(R.id.pg);
        initData();
    }

    private void initData() {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                File file = VPNLog.getBaseDir(getApplicationContext());
                File[] files = file.listFiles();
                if (files == null || files.length == 0) {
                    initView();
                    pg.setVisibility(View.GONE);
                    return;
                }
                List<File> fileList = new ArrayList<>();
                Collections.addAll(fileList, files);
                Iterator<File> iterator = fileList.iterator();
                //过滤掉空文件夹
                while (iterator.hasNext()) {
                    File next = iterator.next();
                    if(next.isFile()){
                        iterator.remove();
                    }else {
                        String[] childList = next.list();
                        if (childList == null || childList.length == 0) {
                            iterator.remove();
                        }
                    }
                }
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File o1, File o2) {
                        return (int) (o2.lastModified() - o1.lastModified());
                    }
                });
                mFileName = new ArrayList<>();
               for(int i =0;i<fileList.size();i++){
                   mFileName.add(fileList.get(i).getName());
               }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pg.setVisibility(View.GONE);
                        initView();
                    }
                });
            }
        });
    }

    private void initView() {
        mAdapter = new LogAdapter();
        listView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    class LogAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = View.inflate(LogCollectorActivity.this, R.layout.item_log, null);
            return new CommonHolder(inflate);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((CommonHolder) holder).date.setText(mFileName.get(position));

            ((CommonHolder) holder).delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    pg.setVisibility(View.VISIBLE);

                    ThreadProxy.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            File logFile = new File(VPNLog.getBaseDir(getApplicationContext()) , mFileName.get(position));
                            MyFileUtils.deleteFile(logFile, null);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    pg.setVisibility(View.GONE);
                                    mFileName.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                }
            });
            ((CommonHolder) holder).share.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pg.setVisibility(View.VISIBLE);
                    ThreadProxy.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            File resultFile = new File(VPNLog.getBaseDir(getApplicationContext())
                                    , "log_" + mFileName.get(position)  + ".zip");
                            String resultPath = null;
                            if(!resultFile.exists()){
                                File logFile = new File(VPNLog.getBaseDir(getApplicationContext()) , mFileName.get(position));
                                File resultTempFile = new File(VPNLog.getBaseDir(getApplicationContext())
                                        , "log_" + mFileName.get(position) + "_temp" + ".zip");
                                String filePath = resultTempFile.getAbsolutePath();
                                try {
                                    MyFileUtils.zipFolder(logFile.getAbsolutePath(), filePath);
                                } catch (Exception e) {
                                    VPNLog.e(LOG_TAG, "failed to zip e = " + e.getMessage());
                                    filePath = null;
                                }
                                if(filePath!=null){
                                    resultTempFile.renameTo(resultFile);
                                    resultPath=resultFile.getAbsolutePath();
                                }
                            }else{
                                resultPath = resultFile.getAbsolutePath();
                            }
                            String finalFilePath = resultPath;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pg.setVisibility(View.GONE);
                                    if(finalFilePath==null){
                                        Toast.makeText(LogCollectorActivity.this
                                                ,getApplicationContext().getText(R.string.failed_to_share)
                                                ,Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    ContextUtil.shareFile(LogCollectorActivity.this, new File(finalFilePath));
                                }
                            });
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFileName == null ? 0 : mFileName.size();
        }

        class CommonHolder extends RecyclerView.ViewHolder {
            TextView date;
            TextView delete;
            TextView share;

            public CommonHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.date);
                delete = itemView.findViewById(R.id.delete);
                share = itemView.findViewById(R.id.share);
            }
        }
    }
}
