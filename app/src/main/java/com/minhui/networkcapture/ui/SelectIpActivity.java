package com.minhui.networkcapture.ui;

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
import com.minhui.vpn.utils.ACache;
import com.minhui.vpn.utils.ThreadProxy;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/6/9.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class SelectIpActivity extends BaseActivity {
    @BindView(R.id.ip)
    EditText ip;
    @BindView(R.id.add)
    TextView add;
    @BindView(R.id.add_container)
    RelativeLayout addContainer;
    @BindView(R.id.ip_list)
    ListView ipList;
    private ArrayList<String> selectIPs;
    private ArrayList<CheckIP> checkIPS = new ArrayList<>();
    private IPAdapter ipAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectIPs = (ArrayList<String>) ACache
                .get(getApplication())
                .getAsObject(AppConstants.SELECT_IP);
        if (selectIPs != null) {
            for (String ip : selectIPs) {
                CheckIP checkIP = new CheckIP(ip, true);
                checkIPS.add(checkIP);
            }
        }
        ipAdapter = new IPAdapter();
        ipList.setAdapter(ipAdapter);
    }

    @OnClick(R.id.add)
    public void addIP() {
        String needAddIP = ip.getText().toString().trim().replace(" ", "");
        if (TextUtils.isEmpty(needAddIP)) {
            return;
        }
        for (CheckIP ip : checkIPS) {
            if (needAddIP.equals(ip.ip)) {
                ip.isChecked = true;
                ipAdapter.notifyDataSetChanged();
                return;
            }
        }
        checkIPS.add(0, new CheckIP(needAddIP, true));
        ipAdapter.notifyDataSetChanged();
        savaData();
    }

    class IPAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return checkIPS == null ? 0 : checkIPS.size();
        }

        @Override
        public Object getItem(int position) {
            return checkIPS.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
              convertView  =View.inflate(SelectIpActivity.this,R.layout.item_ip,null);
            }
            CheckBox checkBox = convertView.findViewById(R.id.ip_item);
            CheckIP checkIP = checkIPS.get(position);
            checkBox.setChecked(checkIP.isChecked);
            checkBox.setText(checkIP.ip);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkIP.isChecked=false;
                    savaData();
                }
            });
            return convertView;
        }
    }

    static class CheckIP {
        String ip;
        boolean isChecked;

        public CheckIP(String ip, boolean isChecked) {
            this.ip = ip;
            this.isChecked = isChecked;
        }
    }
    private void savaData(){
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> saveIP = new ArrayList<>();
                for (CheckIP ip : checkIPS) {
                    if (ip.isChecked) {
                        saveIP.add(ip.ip);
                    }
                }
                ACache.get(getApplicationContext()).put(AppConstants.SELECT_IP, saveIP);
            }
        });
    }


    @Override
    protected int getLayout() {
        return R.layout.activity_ip_select;
    }
}
