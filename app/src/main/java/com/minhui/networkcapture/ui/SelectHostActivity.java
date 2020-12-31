package com.minhui.networkcapture.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseActivity;
import com.minhui.networkcapture.utils.AppConstants;
import com.minhui.networkcapture.view.CheckableImageView;
import com.minhui.vpn.utils.ACache;
import com.minhui.vpn.utils.ThreadProxy;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/9.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class SelectHostActivity extends BaseActivity {
    @BindView(R.id.ip)
    EditText hostET;
    @BindView(R.id.add)
    TextView add;
    @BindView(R.id.add_container)
    RelativeLayout addContainer;
    @BindView(R.id.ip_list)
    ListView hostList;
    @BindView(R.id.auto_match)
    CheckableImageView autoMatch;
    private ArrayList<String> selectHosts;
    private ArrayList<CheckHost> checkHost = new ArrayList<>();
    private HostAdapter hostAdapter;
    private SharedPreferences sp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectHosts = (ArrayList<String>) ACache
                .get(getApplication())
                .getAsObject(AppConstants.SELECT_HOST);
        hostET.setHint(getString(R.string.select_host));
        if (selectHosts != null) {
            for (String host : selectHosts) {
                CheckHost checkHost = new CheckHost(host, true);
                this.checkHost.add(checkHost);
            }
        }
        hostAdapter = new HostAdapter();
        hostList.setAdapter(hostAdapter);
        sp = getSharedPreferences(AppConstants.DATA_SAVE, MODE_PRIVATE);
        boolean autoMatchIP = sp.getBoolean(AppConstants.AUTO_MATCH_HOST, true);
        autoMatch.setChecked(autoMatchIP);
    }
    @OnClick(R.id.auto_match)
    public void switchAutoMatch(){
        boolean checked = !autoMatch.isChecked();
        autoMatch.setChecked(checked);
        sp.edit().putBoolean(AppConstants.AUTO_MATCH_HOST,checked).apply();
    }
    @OnClick(R.id.add)
    public void addHost() {
        String needAddIP = hostET.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(needAddIP)) {
            return;
        }
        for (CheckHost ip : checkHost) {
            if (needAddIP.equals(ip.host)) {
                ip.isChecked = true;
                hostAdapter.notifyDataSetChanged();
                return;
            }
        }
        checkHost.add(0, new CheckHost(needAddIP, true));
        hostAdapter.notifyDataSetChanged();
        saveData();
    }

    class HostAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return checkHost == null ? 0 : checkHost.size();
        }

        @Override
        public Object getItem(int position) {
            return checkHost.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(SelectHostActivity.this, R.layout.item_ip, null);
            }
            CheckBox checkBox = convertView.findViewById(R.id.ip_item);
            CheckHost checkIP = checkHost.get(position);
            checkBox.setChecked(checkIP.isChecked);
            checkBox.setText(checkIP.host);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkIP.isChecked = false;
                    saveData();
                }
            });
            return convertView;
        }
    }

    static class CheckHost {
        String host;
        boolean isChecked;

        public CheckHost(String host, boolean isChecked) {
            this.host = host;
            this.isChecked = isChecked;
        }
    }

    private void saveData() {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> saveHost = new ArrayList<>();
                for (CheckHost host : checkHost) {
                    if (host.isChecked) {
                        saveHost.add(host.host);
                    }
                }
                ACache.get(getApplicationContext()).put(AppConstants.SELECT_HOST, saveHost);
            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_host_select;
    }
}
