package com.minhui.vpn.parser;

import android.text.TextUtils;

import androidx.annotation.Keep;

import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.Conversation;
import com.minhui.vpn.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;


/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/7/2.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 * 本类的作用是将一条连接下的所有文件进行解析
 */
@Keep
public class HttpFileParser {
    private static final String TAG = "HttpFileParser";
    private static final String CONTENT_TYPE = "Content-Type".toUpperCase();
    String requestStr;
    List<File> mFilesList;
    boolean DEBUG = true;
    Conversation mConversation;

    public HttpFileParser(Conversation conversation) {
        mConversation= conversation;
        mFilesList = conversation.getShowDataFile();
    }

    private List<ShowData> parseSaveFiles(File childFile) {
        ArrayList<ShowData> showDataList = new ArrayList<>();

        String name = childFile.getName();
        boolean isRequest = name.contains(TcpDataSaveHelper.REQUEST);

        Source fileSource = null;
        BufferedSource buffer = null;
        try {
            fileSource = Okio.source(childFile);
            buffer = Okio.buffer(fileSource);
            while (!buffer.exhausted()) {
                ShowData addShowData = parseSource(buffer, isRequest);
                if (addShowData == null) {
                    break;
                }

                if (isRequest) {
                    addShowData.parseRequestStr();
                    requestStr = addShowData.mPathUrl;
                }

                //要确保有数据
                if (!TextUtils.isEmpty(addShowData.headStr)
                        || !TextUtils.isEmpty(addShowData.bodyStr)) {
                    showDataList.add(addShowData);
                }
            }
        } catch (Exception e) {
            VPNLog.e(TAG, "failed parseSaveFiles " + e.getMessage());
            e.printStackTrace();
        } finally {
            Utils.close(fileSource);
            Utils.close(buffer);
        }

        return showDataList;

    }

    private ShowData parseSource(BufferedSource buffer, boolean isRequest) throws Exception {
        HeadData headData = null;

        headData = HeadData.readHeadData(buffer);

        if (headData == null) {
            VPNLog.w(TAG, "parseSource no headData");
            return null;
        }
        ShowData showData = new ShowData(mConversation);
        showData.isRequest = isRequest;
        if (!isRequest) {
            showData.mPathUrl = requestStr;
        }
        VPNLog.d(TAG, "parseSource isRequest = " + isRequest + " showData.requestStr = " + showData.mPathUrl);
        showData.headStr = headData.headStr;
        showData.setHeadData(headData);
        int contentLength = headData.contentLength;

        List<SourceIntercept> bufferIntercepts = headData.getBufferIntercepts();
        for (SourceIntercept intercept : bufferIntercepts) {
            buffer = intercept.intercept(buffer);
        }
        //检查 防止实际获得的数据小于应获得的数据而出现解析错误
        if (buffer.buffer().size() < contentLength) {
            contentLength = 0;
        }
        String contentType = headData.heads.get(CONTENT_TYPE);
        if (contentType != null) {
            showData.consumeContent(contentType, buffer, contentLength);

        }
        return showData;
    }

    public List<ShowData> getShowDataFromDir() {
        List<ShowData> showDataList = new ArrayList<>();
        for (File childFile : mFilesList) {
            List<ShowData> showData = parseSaveFiles(childFile);
            showDataList.addAll(showData);
        }
        return showDataList;
    }

}
