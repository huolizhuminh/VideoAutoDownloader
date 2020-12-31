package com.minhui.vpn.tunnel;

import android.os.Handler;
import android.os.Looper;

import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.greenDao.DaoSession;
import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.parser.TcpDataSaveHelper;
import com.minhui.vpn.utils.MyFileUtils;
import com.minhui.vpn.utils.ThreadProxy;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author minhui.zhu
 * Created by minhui.zhu on 2018/6/30.
 * Copyright © 2017年 Oceanwing. All rights reserved.
 */
public class NetworkDataHandlerDelegate implements DataHandler {
    private static final String TAG = "NetworkDataHandlerDelegate";
    private final TcpDataSaveHelper helper;
    private NatSession session;
    private Handler handler;
    private boolean hasRefreshAppBlock = false;

    public NetworkDataHandlerDelegate(NatSession session) {
        this.session = session;
        String helperDir = session.getSaveDataDir();
        handler = new Handler(Looper.getMainLooper());
        helper = new TcpDataSaveHelper(session, helperDir);
    }

    @Override
    public void beforeSend(ByteBuffer buffer) {
        if (!session.needCapture()) {
            VPNLog.d(TAG, "!session.needCapture " + session.getRemoteHost());
            return;
        }
        if(!session.canParse()){
            return;
        }
        byte[] array = buffer.array();
        int offSet = session.isUDP() ? UDPTunnel.HEADER_SIZE : 0;
        byte[] newArray = Arrays.copyOfRange(array, offSet, buffer.limit());
        TcpDataSaveHelper.SaveData saveData = new TcpDataSaveHelper
                .SaveData
                .Builder()
                .isRequest(true)
                .needParseData(newArray)
                .build();
        helper.addData(saveData);
        // refreshAppInfo();
        session.rawBytesSent += newArray.length;
        session.rawPacketSent++;
    }

    @Override
    public void afterReceived(ByteBuffer buffer) {
        if (!session.needCapture()) {
            VPNLog.d(TAG, "!session.needCapture " + session.getRemoteHost());
            return;
        }
        if(!session.canParse()){
            return;
        }
        byte[] array = buffer.array();
        int offSet = session.isUDP() ? UDPTunnel.HEADER_SIZE : 0;
        byte[] newArray = Arrays.copyOfRange(array, offSet, buffer.limit());
        TcpDataSaveHelper.SaveData saveData = new TcpDataSaveHelper
                .SaveData
                .Builder()
                .isRequest(false)
                .needParseData(newArray)
                .build();
        helper.addData(saveData);
        session.rawReceiveByteNum += newArray.length;
        session.rawReceivePacketNum++;
        refreshAppInfo();
    }

    private void refreshAppInfo() {
        if (session.pgName != null) {
            return;
        }
        if (hasRefreshAppBlock) {
            return;
        }
        hasRefreshAppBlock = true;
        session.refreshUID();
    }


    @Override
    public void onDispose() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ThreadProxy.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (session.rawBytesSent == 0 && session.rawReceiveByteNum == 0) {
                            return;
                        }
                        helper.onConversationFinished();
                        MyFileUtils.deleteFile(new File(session.getSaveDataDir()),null);
                        MyFileUtils.deleteFile(new File(session.getParseDataDir()),null);
                    }
                });
            }
        }, 1000);
    }
}
