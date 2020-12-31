package com.minhui.vpn.processparse;

import android.content.Context;

import com.minhui.vpn.nat.NatSession;

import static android.os.Process.INVALID_UID;

public class EmptyUIDDumper implements UIDDumper {
    @Override
    public int getUid(Context context, NatSession session) {
        return INVALID_UID;
    }

    @Override
    public void init(Context context) {

    }
}
