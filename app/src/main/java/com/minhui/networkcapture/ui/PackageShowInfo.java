package com.minhui.networkcapture.ui;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/4/30.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class PackageShowInfo implements Serializable {
    private static final java.lang.String NO_APP_NAME = "COM.";
    public String appName;
    public String packageName;
    public long packageSelectTime;
    //public transient ApplicationInfo applicationInfo;

    public static ArrayList<PackageShowInfo> getPackageShowInfo(Context context) {
        ArrayList<PackageShowInfo> showInfos = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);

        for (PackageInfo info : installedPackages) {
            PackageShowInfo packageShowInfo = new PackageShowInfo();
            packageShowInfo.packageName = info.packageName;

            packageShowInfo.appName = (String) info.applicationInfo.loadLabel(packageManager);

            //  packageShowInfo.applicationInfo = info.applicationInfo;
            showInfos.add(packageShowInfo);

        }
        return showInfos;
    }

    public static class MyComparator implements Comparator<PackageShowInfo> {
        @Override
        public int compare(PackageShowInfo o1, PackageShowInfo o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1.packageSelectTime != o2.packageSelectTime) {
                return o2.packageSelectTime - o1.packageSelectTime > 0 ? 1 : -1;
            }
            if (o1.appName == null && o2.appName == null) {
                return o1.packageName.toUpperCase().compareTo(o2.packageName.toUpperCase());
            }

            if (o1.appName == null) {
                return -1;
            }
            if (o2.appName == null) {
                return 1;
            }
            if (o1.appName.toUpperCase().startsWith(NO_APP_NAME) &&
                    !o2.appName.toUpperCase().startsWith(NO_APP_NAME)) {
                return 1;
            }
            if (!o1.appName.toUpperCase().startsWith(NO_APP_NAME) &&
                    o2.appName.toUpperCase().startsWith(NO_APP_NAME)) {
                return -1;
            }
            return o1.appName.toUpperCase().compareTo(o2.appName.toUpperCase());
        }
    }
}
