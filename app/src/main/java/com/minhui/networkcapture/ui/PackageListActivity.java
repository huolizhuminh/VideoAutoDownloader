package com.minhui.networkcapture.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.annotation.Nullable;

import com.minhui.networkcapture.R;
import com.minhui.networkcapture.base.BaseActivity;
import com.minhui.networkcapture.utils.AppConstants;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.utils.ACache;
import com.minhui.vpn.utils.ThreadProxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.minhui.networkcapture.utils.AppConstants.SELECT_PACKAGE;


/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/2/27.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class PackageListActivity extends BaseActivity {


    private static final String TAG = "PackageListActivity";
    private ListView packageListView;
    private ArrayList<PackageShowInfo> packageShowInfo;
    PackageManager pm;
    private ProgressBar pg;
    private ShowPackageAdapter showPackageAdapter;
    private EditText searchView;
    private ListView searchList;
    private ShowPackageAdapter searchPackageAdapter;
    private ArrayList<PackageShowInfo> searchShowInfos;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_package_list);
        pg = (ProgressBar) findViewById(R.id.pg);

        pm = getPackageManager();
        packageListView = (ListView) findViewById(R.id.package_list);
        getDataAndRefreshView();
        packageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    PackageShowInfo showInfo = packageShowInfo.get(position - 1);
                    showInfo.packageSelectTime = System.currentTimeMillis();

                //    intent.putExtra(SELECT_PACKAGE, showInfo);
                    ThreadProxy.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            ACache.get(getApplication()).put(AppConstants.SHOW_PACKAGE_LIST, packageShowInfo);
                            ACache.get(getApplication()).put(SELECT_PACKAGE, showInfo);
                        }
                    });
                }else {
                    ACache.get(getApplicationContext()).remove(SELECT_PACKAGE);
                }
             //   setResult(RESULT_OK, intent);
                finish();
            }
        });

        searchView = findViewById(R.id.search);
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                refreshViewOnSearchChange(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchList = findViewById(R.id.search_list);
        searchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position != 0) {
                    PackageShowInfo showInfo = searchShowInfos.get(position - 1);
                    showInfo.packageSelectTime = System.currentTimeMillis();

                    ThreadProxy.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            ACache.get(getApplication()).put(AppConstants.SHOW_PACKAGE_LIST, packageShowInfo);
                            ACache.get(getApplication()).put(SELECT_PACKAGE, showInfo);
                        }
                    });
                }else {
                    ACache.get(getApplicationContext()).remove(SELECT_PACKAGE);
                }

                finish();
            }
        });
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_package_list;
    }

    private void saveData() {
        final Context context = getApplicationContext();
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                ACache.get(context).put(AppConstants.SHOW_PACKAGE_LIST, packageShowInfo);
            }
        });
    }

    private void refreshViewOnSearchChange(CharSequence s) {
        if (TextUtils.isEmpty(s)) {
            searchList.setVisibility(View.GONE);
            packageListView.setVisibility(View.VISIBLE);
            return;
        }else {
            searchList.setVisibility(View.VISIBLE);
            packageListView.setVisibility(View.GONE);
        }
        if (packageShowInfo == null) {
            return;
        }
        String searchStr = s.toString().toLowerCase();
        searchShowInfos = new ArrayList<>();
        for (PackageShowInfo info : packageShowInfo) {
            if (info.appName != null && info.appName.toLowerCase().contains(searchStr)) {
                searchShowInfos.add(info);
            }
        }
        if (searchPackageAdapter == null) {
            searchPackageAdapter = new ShowPackageAdapter(searchShowInfos);
            searchList.setAdapter(searchPackageAdapter);
        } else {
            searchPackageAdapter.setData(searchShowInfos);
            searchPackageAdapter.notifyDataSetChanged();
        }

    }

    private void getDataAndRefreshView() {
        ThreadProxy.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Object asObject = ACache.get(getApplicationContext()).getAsObject(AppConstants.SHOW_PACKAGE_LIST);
                ArrayList<PackageShowInfo> cacheShowInfos = null;
                if (asObject != null) {
                    cacheShowInfos = (ArrayList<PackageShowInfo>) asObject;
                    Collections.sort(cacheShowInfos, new PackageShowInfo.MyComparator());
                    refreshView(cacheShowInfos);
                }

                ArrayList<PackageShowInfo> packageShowInfo = PackageShowInfo.getPackageShowInfo(getApplicationContext());
                if(cacheShowInfos!=null){
                    for(PackageShowInfo showInfo:packageShowInfo){
                        for(PackageShowInfo cacheInfo:cacheShowInfos){
                            if(cacheInfo.packageName!=null&&cacheInfo.packageName.equals(showInfo.packageName)){
                                showInfo.packageSelectTime=cacheInfo.packageSelectTime;
                                break;
                            }
                        }
                    }
                }
                Collections.sort(packageShowInfo,new PackageShowInfo.MyComparator());
                refreshView(packageShowInfo);
            }
        });
    }

    private void refreshView(ArrayList<PackageShowInfo> infos) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                packageShowInfo = infos;
                if (showPackageAdapter == null) {
                    showPackageAdapter = new ShowPackageAdapter(packageShowInfo);
                    packageListView.setAdapter(showPackageAdapter);
                    pg.setVisibility(View.GONE);
                } else {
                    showPackageAdapter.setData(packageShowInfo);
                    showPackageAdapter.notifyDataSetChanged();
                }
            }
        });


    }


    class ShowPackageAdapter extends BaseAdapter {
        int ALL = 0;
        int COMMON = 1;
        Drawable defaultDrawable;
        List<PackageShowInfo> infos;

        ShowPackageAdapter(List<PackageShowInfo> infos) {
            this.infos = infos;
            defaultDrawable = getResources().getDrawable(R.drawable.sym_def_app_icon);
        }

        void setData(List<PackageShowInfo> infos) {
            this.infos = infos;
        }

        @Override
        public int getCount() {
            return infos == null ? 0 : infos.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Holder holder;
            if (convertView == null) {
                convertView = View.inflate(PackageListActivity.this, R.layout.item_select_package, null);
                holder = new Holder(convertView, position);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
                holder.holderPosition = position;
            }
            if (position == 0) {
                refreshAll(holder);
                return convertView;
            }
            final PackageShowInfo packageShowInfo = infos.get(position - 1);
            if (TextUtils.isEmpty(packageShowInfo.appName)) {
                holder.appName.setText(packageShowInfo.packageName);
            } else {
                holder.appName.setText(packageShowInfo.appName);
            }
            holder.icon.setImageDrawable(defaultDrawable);
            final View alertIconView = convertView;
            ThreadProxy.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    Holder iconHolder = (Holder) alertIconView.getTag();
                    if (iconHolder.holderPosition != position) {
                        return;
                    }
                    Drawable applicationIcon = null;
                    try {
                        applicationIcon = pm.getApplicationIcon(packageShowInfo.packageName);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    Drawable finalDrawable = applicationIcon;
                    // final Drawable drawable = packageShowInfo.applicationInfo.loadIcon(pm);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Holder iconHolder = (Holder) alertIconView.getTag();
                            if (iconHolder.holderPosition != position) {
                                return;
                            }
                            holder.icon.setImageDrawable(finalDrawable);
                        }
                    });
                }
            });
            //      holder.icon.setImageDrawable(packageShowInfo.applicationInfo.loadIcon(pm));
            return convertView;
        }

        private void refreshAll(Holder holder) {
            holder.appName.setText(getString(R.string.all));
            holder.icon.setImageDrawable(null);
        }

        private View getAllView(int position, View convertView, ViewGroup parent) {
            View inflate = View.inflate(PackageListActivity.this, R.layout.item_select_package, null);
            ((TextView) inflate.findViewById(R.id.app_name)).setText(getString(R.string.all));
            return inflate;
        }

        class Holder {
            TextView appName;
            ImageView icon;
            View baseView;
            int holderPosition;

            Holder(View view, int position) {
                baseView = view;
                appName = (TextView) view.findViewById(R.id.app_name);
                icon = (ImageView) view.findViewById(R.id.select_icon);
                this.holderPosition = position;
            }
        }
    }

}
