package com.minhui.vpn.http;

import com.minhui.vpn.log.VPNLog;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/2.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class RequestLineParseData {
    String method;
    String pathUrl;
    String status;

    public String getMethod() {
        return method;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    public String getStatus() {
        return status;
    }

    public static RequestLineParseData parseData(String requestLine) {
        VPNLog.d("parseData requestLine = "+requestLine);
        if (requestLine == null) {
            return null;
        }
        RequestLineParseData requestLineParseData = new RequestLineParseData();

        String[] parts = requestLine.trim().split(" ");
        if (parts.length < 3) {
            requestLineParseData.method = parts[0];
            requestLineParseData.status =parts[1];
            return requestLineParseData;
        }
        requestLineParseData.method = parts[0];
        requestLineParseData.pathUrl =parts[1];
        requestLineParseData.status=parts[2];



        return requestLineParseData;
    }

}
