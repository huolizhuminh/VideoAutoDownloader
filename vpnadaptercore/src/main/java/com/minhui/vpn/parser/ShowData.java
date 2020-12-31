package com.minhui.vpn.parser;

import androidx.annotation.Keep;

import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.VpnServiceHelper;
import com.minhui.vpn.greenDao.DaoSession;
import com.minhui.vpn.greenDao.SessionHelper;
import com.minhui.vpn.http.RequestLineParseData;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.Conversation;
import com.minhui.vpn.utils.MyFileUtils;
import com.minhui.vpn.utils.Utils;
import com.minhui.vpn.utils.VPNDirConstants;
import com.minhui.vpn.video.VideoItem;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Map;

import okio.BufferedSource;

import static com.minhui.vpn.parser.ShowDataType.*;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/28.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
@Keep
public class ShowData {
    private static final String TAG = "ShowData";
    public boolean isRequest;
    String headStr;
    String bodyStr;
    String mediaFormat;
    private String mediaType;
    private HeadData headData;
    public String mPathUrl;
    public int startRange;
    public int endRange;
    private int rangContentLength;
    private String contentType;
    public static final String DEFAULT_CHARSET = "utf-8";
    private static final String CHAR_SET_SHOW = "charset";
    private Conversation mConversation;

    public void setHeadData(HeadData headData) {
        this.headData = headData;
    }

    public ShowData(Conversation conversation) {
        mConversation = conversation;
    }

    public void consumeContent(String contentType, BufferedSource buffer, int contentLength) {
        this.contentType = contentType;
        if (contentType == null) {
            return;
        }
        if (buffer.buffer().size() == 0) {
            return;
        }
        String toLowerCase = contentType.toLowerCase().trim();
        if (toLowerCase.contains(VIDEO_STR) ) {
            parseMediaData(contentType, buffer);
        }
    }

    private void parseMediaData(String contentType, BufferedSource buffer) {
        String[] split = contentType.split("/");
        mediaType = split[0].trim();
        mediaFormat = split[1].trim();
        refreshRange();
        VPNLog.d(TAG, "parseMediaData ");
        if (hasRange()) {
            refreshRangeMediaFile(buffer);
        } else {
            File childFile = new File(new File(VPNDirConstants.ALL_PARSE_VIDEO_PATH), getFullMediaFileName());
            ParseUtil.writeBufferSourceToFile(childFile, buffer);
            MyFileUtils.saveVideoThumbnail(VpnServiceHelper.getContext(), childFile);
            ProxyConfig.Instance.onVideoCaptured(childFile.getAbsolutePath());
            EventBus.getDefault().post(new VideoCapturedEvent(childFile.getAbsolutePath()));
            VPNLog.d(TAG, "onVideoCaptured noRange file = " + childFile.getAbsolutePath());
            insertFile(childFile);
        }
    }

    private void insertFile(File childFile) {
        DaoSession daoSession = SessionHelper.getDaoSession(VpnServiceHelper.getContext(), SessionHelper.VIDEO_TABLE);
        String absolutePath = childFile.getAbsolutePath();
        String requestUrl = mConversation.getSession().getRequestUrl(mPathUrl);
        daoSession.getVideoItemDao().insert(new VideoItem(absolutePath, requestUrl));
        VPNLog.d(TAG, "insertFile path = " + absolutePath + " requestUrl = " + requestUrl);
    }


    private void refreshRangeMediaFile(BufferedSource bufferSource) {
        if (!hasRange()) {
            VPNLog.d(TAG,"refreshRangeMediaFile failedRefresh not has range end = "+endRange +" start = "+startRange);
            return;
        }
        boolean newFile = false;
        String allParsePath = VPNDirConstants.ALL_PARSE_VIDEO_PATH;
        File fileDir = new File(allParsePath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File rangeFile = new File(fileDir, getFullMediaFileName());
        if (!rangeFile.exists()) {
            if (startRange != 0) {
                VPNLog.d(TAG, "refreshRangeMediaFile failedRefresh need new File but not start");
                return;
            }
            VPNLog.d(TAG, "refreshRangeMediaFile new File");
            newFile = true;
        } else {
            VPNLog.d(TAG, "refreshRangeMediaFile no new File");
            if (startRange > rangeFile.length()) {
                VPNLog.d(TAG, "refreshRangeMediaFile failedRefresh byte missed");
                return;
            }
            if (endRange + 1 <= rangeFile.length()) {
                VPNLog.d(TAG, "refreshRangeMediaFile failedRefresh no need refresh ");
                return;
            }
        }
        RandomAccessFile randomAccessFile = null;
        //FileOutputStream outputStream = null;
        byte[] buffer = new byte[1024];
        int readSize = -1;
        try {
            randomAccessFile = new RandomAccessFile(rangeFile, "rw");
            long length = randomAccessFile.length();
            randomAccessFile.seek(length);
            int lastEndRange = (int) rangeFile.length();
            int skip = lastEndRange - startRange;
            bufferSource.skip(skip);
            while ((readSize = bufferSource.read(buffer)) != -1) {
                randomAccessFile.write(buffer, 0, readSize);
            }
        } catch (Exception e) {
            VPNLog.d(TAG, "saveJointData error " + e.getMessage());
        } finally {
            Utils.close(randomAccessFile);
        }
        MyFileUtils.saveVideoThumbnail(VpnServiceHelper.getContext(), rangeFile);
        if (newFile) {
            ProxyConfig.Instance.onVideoCaptured(rangeFile.getAbsolutePath());
            EventBus.getDefault().post(new VideoCapturedEvent(rangeFile.getAbsolutePath()));
            insertFile(rangeFile);
            VPNLog.d(TAG, "onVideoCaptured Range file = " + rangeFile.getAbsolutePath());
        }

    }


    public void parseRequestStr() {
        String headLine = headStr.split("\n")[0];
        RequestLineParseData requestLineParseData = RequestLineParseData.parseData(headLine);
        if (requestLineParseData == null) {
            return;
        }
        mPathUrl = requestLineParseData.getPathUrl();
    }

    public boolean hasRange() {
        return endRange - startRange > 0;
    }

    public boolean isEndRange() {
        return rangContentLength != 0
                && rangContentLength == endRange + 1
                && endRange - startRange > 0;
    }

    public void refreshRange() {
        Map<String, String> heads = headData.heads;
        String prefixRangeAndLength = heads.get(HeadData.CONTENT_RANGE);
        if (prefixRangeAndLength == null) {
            VPNLog.d(TAG, "refreshRange is void ");
            return;
        }
        try {
            String rangeAndLength;
            if (prefixRangeAndLength.toUpperCase().contains(BYTES_STR)) {
                rangeAndLength = prefixRangeAndLength.trim().split(" ")[1];
            } else {
                rangeAndLength = prefixRangeAndLength;
            }
            String[] rangeAndLengthArray = rangeAndLength.split("/");
            String range = rangeAndLengthArray[0];
            String lengthStr = rangeAndLengthArray[1];
            rangContentLength = Integer.parseInt(lengthStr);
            //Content-Range: bytes 819201-3510073/3510074
            String[] rangeArray = range.split("-");
            String rangStartStr = rangeArray[0];
            String rangEndStr = rangeArray[1];
            startRange = Integer.parseInt(rangStartStr);
            endRange = Integer.parseInt(rangEndStr);
            VPNLog.d(TAG, "refreshRange content " + prefixRangeAndLength
                    + " startRange "
                    + startRange
                    + " endRange " + endRange);
        } catch (Exception e) {
            endRange = 0;
            startRange = 0;
            rangContentLength = 0;
        }


    }

    public String getFullMediaFileName() {
        if (mPathUrl == null) {
            return null;
        }
        String fileName = "sslCapture_" + mediaType + "_" + Math.abs(mPathUrl.hashCode()) + "." + mediaFormat;
        VPNLog.d(TAG, "getFullMediaFileName name = " + fileName);
        return fileName;
    }


    public int getContentType() {
        return ShowDataType.getContentType(contentType);
    }

    public String getContentTypeStr() {
        return contentType;
    }
}
