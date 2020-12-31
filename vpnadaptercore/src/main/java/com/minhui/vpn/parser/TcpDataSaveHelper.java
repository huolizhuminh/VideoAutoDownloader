package com.minhui.vpn.parser;

import android.util.Log;

import com.minhui.vpn.http.RequestLineParseData;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.Conversation;
import com.minhui.vpn.nat.ConversationManager;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.utils.MyFileUtils;
import com.minhui.vpn.utils.ThreadProxy;
import com.minhui.vpn.utils.VPNDirConstants;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.internal.Util;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/5/7.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

public class TcpDataSaveHelper {
    private static final boolean LOG_DEBUG = false;
    private static final boolean SAVE_DEBUG = false;
    private String TAG;
    private String dir;
    private SaveData lastSaveData;
    private File lastSaveFile;
    private int nextRequestNum = 0;
    private int nextResponseNum = 0;
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    private NatSession session;
    private ConcurrentLinkedQueue<SaveData> needWriteData = new ConcurrentLinkedQueue<>();
    private boolean hasAddSocketCon = false;
    private boolean mCheckedNotVideoType = false;

    public TcpDataSaveHelper(NatSession session, String dir) {
        this.session = session;
        this.dir = dir;
        TAG = "sdh:" + session.getLocalPortInt();
    }

    /**
     * 异步保存数据
     *
     * @param data 需要保存的数据
     */
    public void addData(final SaveData data) {
        if (mCheckedNotVideoType) {
            return;
        }
        needWriteData.offer(data);
        triggerWrite();
    }

    private void triggerWrite() {
        ThreadProxy.getInstance().executeInSingle(new Runnable() {
            @Override
            public void run() {
                while (!needWriteData.isEmpty()) {
                    SaveData data = needWriteData.poll();
                    if (data == null) {
                        return;
                    }
                    if (lastSaveData == null || (lastSaveData.isRequest ^ data.isRequest)) {
                        newFileAndSaveData(data);
                    } else {
                        appendFileData(data);
                    }
                    lastSaveData = data;
                }

            }
        });
    }

    private void log(String tag, String msg) {
        if (LOG_DEBUG) {
            VPNLog.d(TAG, msg);
        }
    }

    private void appendFileData(SaveData data) {
        log(TAG, "appendFileData " + data.isRequest);
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(lastSaveFile.getAbsolutePath(), "rw");
            long length = randomAccessFile.length();
            randomAccessFile.seek(length);
            randomAccessFile.write(data.needParseData, data.offSet, data.playoffSize);
        } catch (Exception e) {
            VPNLog.e(TAG, "failed to appendFileData " + e.getMessage());
        }
    }


    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                log(TAG, "failed to close closeable");
            }
        }
    }

    private void newFileAndSaveData(SaveData data) {
        log(TAG, "newFileAndSaveData " + data.isRequest);
        int saveNum;
        if (data.isRequest) {
            saveNum = nextRequestNum;

            if (saveNum > 0) {
                onConversationFinished();
            }
            nextRequestNum++;
        } else {
            saveNum = nextResponseNum;
            nextResponseNum++;
        }
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String childName = (data.isRequest ? REQUEST : RESPONSE) + "_" + saveNum;
        lastSaveFile = new File(file, childName);
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(lastSaveFile);
            fileOutputStream.write(data.needParseData, data.offSet, data.playoffSize);
            fileOutputStream.flush();
        } catch (Exception e) {
            VPNLog.e(TAG, "failed to newFileAndSaveData " + e.getMessage());
        } finally {
            close(fileOutputStream);
        }
        //http或者https时增加一条converSation
        if (data.isRequest) {
            onConversationStart();
        }

    }

    /**
     * 表示会话开始
     */
    private void onConversationStart() {
        int startIndex = nextRequestNum - 1;
        if (session.isHttpsRoute || session.isHttp) {
            String url = getLastURL(startIndex);
            VPNLog.d(TAG, "onConversationStart url = " + url);
            session.addConversation(url);
            ConversationManager.getInstance().addConversation(session.getLastConversation());
        } else if (!hasAddSocketCon) {
            VPNLog.d(TAG, "onConversationStart cannot parse");
            hasAddSocketCon = true;
            session.addConversation(session.getRemoteIPStr());
            ConversationManager.getInstance().addConversation(session.getLastConversation());
        }
    }

    /**
     * 会话结束
     * http https为每一次请求的响应得到了回复,socket为断连
     */
    public void onConversationFinished() {
        if (mCheckedNotVideoType) {
            return;
        }
        if (nextResponseNum != nextRequestNum) {
            return;
        }
        int finishIndex;
        if (session.canParse()) {
            finishIndex = nextResponseNum - 1;
        } else {
            finishIndex = 0;
        }
        List<ShowData> showDataList = null;
        Conversation lastConversation = session.getLastConversation();
        if (session.needCapture()) {
            HttpFileParser fileParser = new HttpFileParser(lastConversation);
            showDataList = fileParser.getShowDataFromDir();
            VPNLog.d(TAG, "onConversationFinished lastConversation = " + lastConversation + " showDataList = " + showDataList);
        }
        //解析出此conversation的type.
        int type = ShowDataType.OTHER;
        if (showDataList != null) {
            if (showDataList.size() > 1) {
                ShowData showData = showDataList.get(1);
                type = showData.getContentType();
                if (type == ShowDataType.VIDEO) {
                    copyRawFile(lastConversation.getShowDataFile(), String.valueOf(Math.abs(showDataList.get(0).mPathUrl.hashCode())));
                }
                VPNLog.d(TAG, "onConversationFinished  type = " + type + " typeStr = " + showData.getContentTypeStr());
            }
        }
        if (type != ShowDataType.VIDEO) {
            mCheckedNotVideoType = true;
            clearAll();
        }
        if (session.isUDP()) {
            session.refreshConversation(finishIndex, ShowDataType.UDP);
        } else {
            session.refreshConversation(finishIndex, type);
        }
    }

    private void copyRawFile(List<File> fileList, String dirName) {
        if (SAVE_DEBUG) {
            String rawDir = VPNDirConstants.ALL_PARSE_VIDEO_PATH
                    + dirName
                    + "/";
            File dirFile = new File(rawDir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }

            for (File childFile : fileList) {
                int index = 0;
                boolean isRequest = childFile.getName().contains(TcpDataSaveHelper.REQUEST);
                String[] list = dirFile.list();
                if (list != null && list.length != 0) {
                    for (String fileName : list) {
                        if (isRequest) {
                            if (fileName.contains(TcpDataSaveHelper.REQUEST)) {
                                VPNLog.d(TAG,"copyRawFile is request fileName = "+fileName +" index = "+index);
                                index++;
                            }
                        } else {
                            if (fileName.contains(TcpDataSaveHelper.RESPONSE)) {
                                VPNLog.d(TAG,"copyRawFile is response fileName = "+fileName +" index = "+index);
                                index++;
                            }
                        }
                    }
                }
                String absolutePath = rawDir
                        + (isRequest ? TcpDataSaveHelper.REQUEST : TcpDataSaveHelper.RESPONSE) + "_" + index;
                File desFile = new File(absolutePath);
                MyFileUtils.copyFile(childFile, desFile);
            }

        }
    }

    private void clearAll() {
        needWriteData.clear();
    }

    private String getLastURL(int saveNum) {
        File file = session.getReqSaveDataFile(saveNum);
        FileReader inputStream = null;
        String url = null;
        try {
            inputStream = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(inputStream);
            String readLine = bufferedReader.readLine();
            RequestLineParseData requestLineParseData = RequestLineParseData.parseData(readLine);
            url = session.getRequestUrl(requestLineParseData.getPathUrl());
        } catch (Exception e) {
            Log.e(TAG, "failed to getLastURL " + e.getMessage());
        } finally {
            Util.closeQuietly(inputStream);
        }

        return url;
    }


    public static class SaveData {
        boolean isRequest;
        byte[] needParseData;
        int offSet;
        int playoffSize;

        private SaveData(Builder builder) {
            isRequest = builder.isRequest;
            needParseData = builder.needParseData;
            offSet = builder.offSet;
            if (builder.length == 0) {
                if (needParseData != null) {
                    playoffSize = needParseData.length;
                }
            } else {
                playoffSize = builder.length;
            }

        }


        public static final class Builder {
            private boolean isRequest;
            private byte[] needParseData;
            private int offSet;
            private int length;

            public Builder() {
            }

            public Builder isRequest(boolean val) {
                isRequest = val;
                return this;
            }

            public Builder needParseData(byte[] val) {
                needParseData = val;
                return this;
            }

            public Builder offSet(int val) {
                offSet = val;
                return this;
            }

            public Builder length(int val) {
                length = val;
                return this;
            }

            public SaveData build() {
                return new SaveData(this);
            }
        }
    }

}
