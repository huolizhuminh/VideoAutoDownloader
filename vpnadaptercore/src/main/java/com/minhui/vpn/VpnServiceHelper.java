package com.minhui.vpn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.Keep;

import com.minhui.vpn.log.VPNLog;
import com.minhui.vpn.nat.Conversation;
import com.minhui.vpn.nat.ConversationManager;
import com.minhui.vpn.processparse.DefaultAppManager;
import com.minhui.vpn.processparse.UIDDumper;
import com.minhui.vpn.processparse.UIDDumperFactory;
import com.minhui.vpn.service.CaptureVpnService;
import com.minhui.vpn.utils.TimeFormatUtil;

import java.net.DatagramSocket;
import java.net.Socket;
import java.util.List;

import static com.minhui.vpn.utils.VPNDirConstants.BASE_PARSE;

@Keep
public class VpnServiceHelper {
    private static final String TAG = "VpnServiceHelper";
    private static Context sContext;
    public static final int START_VPN_SERVICE_REQUEST_CODE = 2015;
    private static CaptureVpnService sVpnService;
    private static SharedPreferences sp;
    private static String name;
    public static boolean needCapture = true;
    private static UIDDumper mUIDDumper;

    public static void onVpnServiceCreated(CaptureVpnService vpnService) {
        sVpnService = vpnService;
        if (sContext == null) {
            sContext = vpnService.getApplicationContext();
        }
        mUIDDumper = UIDDumperFactory.createUIDDumper(ProxyConfig.Instance);
        mUIDDumper.init(getContext());
        DefaultAppManager.getsInstance().init(getContext());

    }

    public static UIDDumper getUIDDumper() {
        if (mUIDDumper == null) {
            mUIDDumper = UIDDumperFactory.createUIDDumper(ProxyConfig.Instance);
        }
        return mUIDDumper;
    }

    public static String getName() {
        return name;
    }


    public static void onVpnServiceDestroy() {
        sVpnService = null;
    }

    public static Context getContext() {
        return sContext;
    }


    public static boolean protect(Socket socket) {
        if (sVpnService != null) {
            return sVpnService.protect(socket);
        }
        return false;
    }

    public static boolean protect(DatagramSocket socket) {
        if (sVpnService != null) {
            return sVpnService.protect(socket);
        }
        return false;
    }

    public static boolean vpnRunningStatus() {
        if (sVpnService != null) {
            return sVpnService.vpnRunningStatus();
        }
        return false;
    }

    public static void changeVpnRunningStatus(Context context, boolean isStart, String name) {
        if (context == null) {
            return;
        }
        VpnServiceHelper.name = name;
        if (isStart) {
            Intent intent = null;
            try {
                intent = CaptureVpnService.prepare(context);
                if (intent == null) {
                    startVpnService(context);
                } else {
                    if (context instanceof Activity) {
                        ((Activity) context).startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                    }
                }
            } catch (Exception e) {
                VPNLog.e(TAG, "error changeVpnRunningStatus " + e.getMessage());
            }

        } else if (sVpnService != null) {
            boolean stopStatus = false;
            sVpnService.setVpnRunningStatus(stopStatus);
        }
    }

    public static List<Conversation> getAllConversation() {
        if (CaptureVpnService.lastVpnStartTimeFormat == null) {
            return null;
        }
        return ConversationManager.getInstance().getConversations();
    }

    public static void startVpnService(Context context) {
        if (context == null) {
            return;
        }

        context.startService(new Intent(context, CaptureVpnService.class));


    }

    public static void clearConversation() {
        ConversationManager.getInstance().clear();
    }

    public static void initContext(Context context) {
        sContext = context;
    }
}
