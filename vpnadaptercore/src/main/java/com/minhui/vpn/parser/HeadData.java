package com.minhui.vpn.parser;

import com.minhui.vpn.log.VPNLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okio.BufferedSource;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/28.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class HeadData {
    private static final String TAG = "HeadData";
    Map<String, String> heads = new HashMap<>();
    String headStr;
    int contentLength = 0;
    private static final int HEADER_LIMIT = 256 * 1024;
    private static final String CONTENT_ENCODING = "Content-Encoding".toUpperCase();

    private static final String TRANSFER_TYPE = "Transfer-Encoding".toUpperCase();
    private static final String CONTENT_LENGTH = "Content-Length".toUpperCase();
    public static final String CONTENT_RANGE = "Content-Range".toUpperCase();
    private static final String GZIP = "gzip";
    private static final String BR = "br";
    private static final String CHUNK = "chunked";

    static HeadData readHeadData(BufferedSource buffer) throws Exception {
        long headerLimit = HEADER_LIMIT;
        HeadData headData = new HeadData();
        String line = null;
        //解决粘包时空串问题
        while (line == null || line.length() <= 0) {
            try {
                line = buffer.readUtf8LineStrict(headerLimit);
            } catch (Exception e) {
                try {
                    VPNLog.e(TAG, "failed to readUtf8LineStrict head = " + buffer.buffer().readUtf8());
                } catch (Exception err) {

                }

                return null;
            }
        }
        StringBuilder headBuilder = new StringBuilder();

        while (line != null && line.length() > 0) {
            headerLimit = HEADER_LIMIT - line.length();
            String[] split = line.split(":");
            if (split.length >= 2) {
                headData.heads.put(split[0].toUpperCase(), split[1]);
            }
            headBuilder.append(line).append("\r\n");
            try {
                line = buffer.readUtf8LineStrict(headerLimit);
            } catch (Exception e) {
                VPNLog.e(TAG, "failed to readUtf8LineStrict headStr = " + headBuilder.toString());
                return null;
            }
        }
        headData.headStr = headBuilder.toString();
        String contentLengthStr = headData.heads.get(CONTENT_LENGTH);
        if (contentLengthStr != null) {
            try {
                headData.contentLength = Integer.parseInt(contentLengthStr.trim());
            } catch (Exception e) {
                headData.contentLength = 0;
            }
        }
        return headData;
    }

    List<SourceIntercept> getBufferIntercepts() {
        List<SourceIntercept> sourceIntercept = new ArrayList<>();
        String transferType = heads.get(TRANSFER_TYPE);
        if (transferType != null && CHUNK.equals(transferType.toLowerCase().trim())) {
            sourceIntercept.add(new ChunkedIntercept());
        }
        String encodingType = heads.get(CONTENT_ENCODING);
        if (encodingType != null) {
            String s = encodingType.toLowerCase().trim();
            if (GZIP.equals(s)) {
                sourceIntercept.add(new GzipIntercept());
            }else if(BR.equals(s)){
                sourceIntercept.add(new BrotliIntercept());
            }
        }
        return sourceIntercept;
    }
}
