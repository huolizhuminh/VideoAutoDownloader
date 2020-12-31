package com.minhui.vpn.upload;



import androidx.annotation.Keep;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.Conversation;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.oknet.OKHttpManager;
import com.minhui.vpn.utils.TimeFormatUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.minhui.vpn.nat.NatSession.PARSE_DATA_NAME;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/7.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
@Keep
public class UploadUtil {
    private static final String TAG = "UploadUtil";

    public static void uploadData(Conversation conversation, UpLoadConfig upLoadConfig) {
        uploadData(conversation, upLoadConfig, null);
    }

    public static void uploadData(Conversation conversation, UpLoadConfig upLoadConfig, final UploadListener listener) {
        NatSession session = conversation.getSession();
        String parseDataFile = session.getParseDataFile(conversation.getIndex());
        final File file = new File(parseDataFile);
        if (!file.exists()) {
            if (listener != null) {
                listener.onFailed();
            }
            return;
        }
        if (upLoadConfig == null) {
            if (listener != null) {
                listener.onFailed();
            }
            return;
        }
        final String url = upLoadConfig.getUrl();
        if (url == null) {
            if (listener != null) {
                listener.onFailed();
            }
            return;
        }
        try {
            RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("txt", PARSE_DATA_NAME, fileBody)
                    .build();
            Request.Builder builder = new Request.Builder()
                    .url(upLoadConfig.getUrl())
                    .post(requestBody);
            builder.addHeader(UpLoadConfig.VPN_START_TIME, TimeFormatUtil.formatYYMMDDHHMMSS(session.vpnStartTime))
                    .addHeader(UpLoadConfig.APPLICATION, session.getPGName())
                    .addHeader(UpLoadConfig.IP_AND_PORT, session.getIpAndPortDir());
            HashMap<String, String> addedHeaders = upLoadConfig.getHeaders();
            if (addedHeaders != null) {
                Iterator<Map.Entry<String, String>> iterator = addedHeaders.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    builder.addHeader(next.getKey(), next.getValue());
                }
            }
            Request request = builder.build();
            OKHttpManager.getOkHttpClient().newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (listener != null) {
                                listener.onFailed();
                            }
                            VPNLog.d(TAG, "onFailure " + file.getAbsolutePath());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (listener != null) {
                                listener.onSuccess();
                            }
                            VPNLog.d(TAG, "onResponse " + file.getAbsolutePath());
                        }
                    });
        }catch (Exception e){
            if (listener != null) {
                listener.onFailed();
            }
        }

    }
}






















