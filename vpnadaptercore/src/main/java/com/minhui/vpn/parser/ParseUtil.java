package com.minhui.vpn.parser;

import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/8/3.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class ParseUtil {
    private static final String TAG = "ParseUtil";

    public static void writeBufferSourceToFile(File childFile, BufferedSource buffer) {
        File fileDir =childFile.getParentFile();

        if (!fileDir.exists()) {
            fileDir.mkdirs();

        }

        OutputStream out = null;
        byte[] writeBytes = new byte[1024];
        try {
            out = new FileOutputStream(childFile);
            int read = 0;
            while ((read = buffer.read(writeBytes)) > 0) {
                out.write(writeBytes, 0, read);
            }
            out.flush();
        } catch (Exception e) {
            VPNLog.d(TAG, "failed to write data");
        } finally {
            Utils.close(out);
        }
    }
}
