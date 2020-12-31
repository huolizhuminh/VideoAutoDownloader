package com.minhui.vpn.processparse;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.minhui.vpn.nat.NatSession;

import java.net.InetSocketAddress;

import static android.os.Process.INVALID_UID;
import static android.system.OsConstants.IPPROTO_TCP;
import static android.system.OsConstants.IPPROTO_UDP;
import static com.minhui.vpn.nat.NatSession.UDP;
import static com.minhui.vpn.utils.Utils.convertIp;
import static com.minhui.vpn.utils.Utils.convertPort;

public class ConnectManagerUIDDumper implements  UIDDumper {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int getUid(Context context, NatSession session) {
        int proto = IPPROTO_TCP;
        if (UDP.equals(session.netType)) {
            proto = IPPROTO_UDP;
        }
        InetSocketAddress remoteInetSocketAddress = new InetSocketAddress(convertIp(session.remoteIP), convertPort(session.remotePort));
        InetSocketAddress localInetSocketAddress = new InetSocketAddress(convertIp(session.sourceIP), convertPort(session.localPort));
        int uid = INVALID_UID;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return uid;
        }
        uid = connectivityManager.getConnectionOwnerUid(proto, localInetSocketAddress, remoteInetSocketAddress);

        return uid;
    }

    @Override
    public void init(Context context) {

    }
}
