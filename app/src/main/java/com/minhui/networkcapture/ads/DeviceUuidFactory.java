package com.minhui.networkcapture.ads;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/15.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class DeviceUuidFactory {
    protected static final String PREFS_FILE = "device_id";

    protected static final String PREFS_DEVICE_ID = "device_id";

    protected static UUID uuid;

    private static String deviceType = "0";

    private static final String TYPE_ANDROID_ID = "1";

    private static final String TYPE_DEVICE_ID = "2";

    private static final String TYPE_RANDOM_UUID = "3";

    public static  UUID getDeviceUuid(Context context) {
        if (uuid == null) {
            synchronized (DeviceUuidFactory.class) {
                if (uuid == null) {
                    final SharedPreferences prefs = context.getSharedPreferences(PREFS_FILE, 0);
                    final String id = prefs.getString(PREFS_DEVICE_ID, null);
                    if (id != null) {
                        uuid = UUID.fromString(id);
                    } else {
                        final String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        try {
                            if (!"9774d56d682e549c".equals(androidId)) {
                                deviceType = TYPE_ANDROID_ID;
                                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                            } else {
                                 String deviceId=null;
                                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                     deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                                }

                                if (deviceId != null
                                        && !"0123456789abcdef".equals(deviceId.toLowerCase())
                                        && !"000000000000000".equals(deviceId.toLowerCase())) {
                                    deviceType = TYPE_DEVICE_ID;
                                    uuid = UUID.nameUUIDFromBytes(deviceId.getBytes("utf8"));
                                } else {
                                    deviceType = TYPE_RANDOM_UUID;
                                    uuid = UUID.randomUUID();
                                }
                            }
                        } catch (UnsupportedEncodingException e) {
                            deviceType = TYPE_RANDOM_UUID;
                            uuid = UUID.randomUUID();
                        } finally {
                            uuid = UUID.fromString(deviceType + uuid.toString());
                        }

                        prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).apply();
                    }
                }
            }
        }
        return uuid;
    }
    public static  String getCode(Context context){
        UUID deviceUuid = getDeviceUuid(context);
        return String.valueOf(Math.abs(deviceUuid.getLeastSignificantBits()));
    }

}
