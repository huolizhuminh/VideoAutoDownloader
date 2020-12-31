package com.minhui.vpn.processparse;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.Keep;

import com.minhui.vpn.R;
import com.minhui.vpn.log.VPNLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/4/30.
 *         Copyright © 2017年 minhui.zhu. All rights reserved.
 */
@Keep
public class AppInfo implements Serializable {
    private static final long serialVersionUID = 1;
    private static final String TAG = "AppInfoDATA";
    private static Drawable defaultIcon = null;
    private static final LruCache<String, IconInfo> iconCache = new LruCache(50);
    /**
     * 应用名
     */
    public final String allAppName;
    /**
     * 包名集合
     */
    public final String leaderAppName;
    /**
     * 包名集合
     */
    public final PackageNames pkgs;
    public boolean isSystem = false;

    @Override
    public String toString() {
        return
                "allAppName=" + allAppName + '\n' +
                        "leaderAppName='" + leaderAppName + '\n';
    }

    static class Entry {
        final String appName;
        final String pkgName;

        public Entry(String appName, String pkgName) {
            this.appName = appName;
            this.pkgName = pkgName;
        }
    }

    static class IconInfo {
        long date;
        Drawable icon;

        IconInfo() {
        }
    }


    private AppInfo(String leaderAppName, String allAppName, String[] pkgs, boolean isSystem) {
        this.leaderAppName = leaderAppName;
        this.allAppName = allAppName;
        this.pkgs = PackageNames.newInstance(pkgs);
        this.isSystem = isSystem;
    }

    public static AppInfo createFromUid(Context ctx, int uid) {
        boolean isSystem = true;
        PackageManager pm = ctx.getPackageManager();
        ArrayList<Entry> list = new ArrayList();
        if (uid > 0) {
            try {
                String[] pkgNames = pm.getPackagesForUid(uid);
                if (pkgNames == null || pkgNames.length <= 0) {
                    list.add(new Entry("System", "nonpkg.noname"));
                } else {
                    for (String pkgName : pkgNames) {
                        if (pkgName != null) {
                            try {
                                PackageInfo appPackageInfo = pm.getPackageInfo(pkgName, 0);
                                isSystem = isSystemApp(appPackageInfo);
                                String appName = null;
                                if (appPackageInfo != null) {
                                    appName = appPackageInfo.applicationInfo.loadLabel(pm).toString();
                                }
                                if (appName == null || appName.equals("")) {
                                    appName = pkgName;
                                }
                                list.add(new Entry(appName, pkgName));
                            } catch (PackageManager.NameNotFoundException e) {
                                list.add(new Entry(pkgName, pkgName));
                            }
                        }
                    }
                }
            } catch (RuntimeException e2) {
                Log.i("NRFW", "error getPackagesForUid(). package manager has died");
                return null;
            }
        }
        if (list.size() == 0) {
            list.add(new Entry("System", "root.uid=0"));
        }
        Collections.sort(list, new Comparator<Entry>() {
            public int compare(Entry lhs, Entry rhs) {
                int ret = lhs.appName.compareToIgnoreCase(rhs.appName);
                if (ret == 0) {
                    return lhs.pkgName.compareToIgnoreCase(rhs.pkgName);
                }
                return ret;
            }
        });
        String[] pkgs = new String[list.size()];
        String[] apps = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            pkgs[i] = ((Entry) list.get(i)).pkgName;
            apps[i] = ((Entry) list.get(i)).appName;
        }
        return new AppInfo(apps[0], TextUtils.join(",", apps), pkgs, isSystem);
    }

    public static boolean isSystemApp(PackageInfo pInfo) {
        boolean isSystem = ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
      //  VPNLog.d(TAG, "isSystemApp " + isSystem + "pm" + pInfo.packageName);
        return isSystem;
    }

    public static String getAppName(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo appPackageInfo = null;
        try {
            appPackageInfo = pm.getPackageInfo(pkgName, 0);
            String appName = null;
            if (appPackageInfo != null) {
                appName = appPackageInfo.applicationInfo.loadLabel(pm).toString();
            }
            if (appName == null || appName.equals("")) {
                appName = pkgName;
            }
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getIcon(Context ctx, String pkgName) {
        return getIcon(ctx, pkgName, false);
    }

    public static synchronized Drawable getIcon(Context ctx, String pkgName, boolean onlyPeek) {
        Drawable drawable = null;
        synchronized (AppInfo.class) {
            IconInfo iconInfo;
            if (defaultIcon == null) {
                defaultIcon = ctx.getResources().getDrawable(R.drawable.sym_def_app_icon);
            }
            PackageManager pm = ctx.getPackageManager();
            PackageInfo appPackageInfo = null;
            try {
                appPackageInfo = pm.getPackageInfo(pkgName, 0);
                long lastUpdate = appPackageInfo.lastUpdateTime;
                iconInfo = (IconInfo) iconCache.get(pkgName);
                if (iconInfo != null && iconInfo.date == lastUpdate && iconInfo.icon != null) {
                    drawable = iconInfo.icon;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
            if (appPackageInfo != null) {
                if (!onlyPeek) {
                    drawable = appPackageInfo.applicationInfo.loadIcon(pm);
                    iconInfo = new IconInfo();
                    iconInfo.date = appPackageInfo.lastUpdateTime;
                    iconInfo.icon = drawable;
                    iconCache.put(pkgName, iconInfo);
                }
            } else {
                iconCache.remove(pkgName);
                drawable = defaultIcon;
            }
        }
        return drawable;
    }
}
