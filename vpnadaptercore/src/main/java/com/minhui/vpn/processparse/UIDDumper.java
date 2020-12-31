package com.minhui.vpn.processparse;

import android.content.Context;

import com.minhui.vpn.nat.NatSession;

public interface UIDDumper {
    int getUid(Context context, NatSession session);
    void init(Context context);
}
