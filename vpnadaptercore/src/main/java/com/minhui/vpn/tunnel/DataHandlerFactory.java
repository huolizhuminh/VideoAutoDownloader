package com.minhui.vpn.tunnel;

import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.nat.NatSession;

public class DataHandlerFactory {
    public static DataHandler createHandler(NatSession session) {
        if (!ProxyConfig.Instance.isSaveData()) {
            return new EmptyDataHandler();
        } else if (session.isOwnerConn()) {
            return new EmptyDataHandler();
        } else {
            return new NetworkDataHandlerDelegate(session);
        }
    }
}
