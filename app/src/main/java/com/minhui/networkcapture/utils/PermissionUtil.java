package com.minhui.networkcapture.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.minhui.vpn.log.VPNLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 2017/3/1.
 */

public class PermissionUtil {
    // 权限已经获取
    public static final int PERMISSION_GRANTED = 0;
    // 权限已经被永久拒绝
    public static final int PERMISSION_DENIED_FOREVER = 1;
    // 权限被拒绝，但调用requestPermissions()可以再次弹出对话框
    public static final int PERMISSION_DENIED_ONETIME = 2;
    private static final String TAG = "PermissionUtil";
    public static final String[] READ_WRITE_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static boolean isPermissionGranted(Context context, String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPerms(Context context, String[] arrPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        for (String permission : arrPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }


    /**
     * 获取需要的权限
     *
     * @return 若不需要获取权限，则返回null；否则返回需要获取的权限数组
     */
    public static String[] getRequirePermissions(Activity activity, String[] arrPermissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null;

        List<String> permissionsList = new ArrayList<>();
        for (String permission : arrPermissions) {
            int permissionState = ContextCompat.checkSelfPermission(activity, permission);
            VPNLog.d(TAG, "getRequirePermissions permission = " + permission + " state = " + permissionState);
            if (permissionState != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }

        int N = permissionsList.size();
        return N < 1 ? null : permissionsList.toArray(new String[N]);
    }

    /**
     * 关于shouldShowRequestPermissionRationale函数的一点儿注意事项：
     * (1).应用安装后第一次访问，则直接返回false；
     * (2).第一次请求权限时，用户Deny了，再次调用shouldShowRequestPermissionRationale()，则返回true；
     * (3).第二次请求权限时，用户Deny了，并选择了“never ask again”的选项时，再次调用shouldShowRequestPermissionRationale()时，返回false；
     * (4).设备的系统设置中，禁止了应用获取这个权限的授权，则调用shouldShowRequestPermissionRationale()，返回false。
     */
    public static boolean showRationaleUI(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }


    public static class PermStatus {
        public int status;
        public String[] perms;

        public PermStatus(int status, String[] perms) {
            this.status = status;
            this.perms = perms;
        }

        @Override
        public String toString() {
            return "PermStatus{" +
                    "status=" + status +
                    ", perms=" + Arrays.toString(perms) +
                    '}';
        }
    }
}
