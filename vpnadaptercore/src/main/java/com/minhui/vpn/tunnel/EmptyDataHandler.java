package com.minhui.vpn.tunnel;

import java.nio.ByteBuffer;

public class EmptyDataHandler implements DataHandler {
    @Override
    public void beforeSend(ByteBuffer buffer) {

    }

    @Override
    public void afterReceived(ByteBuffer buffer) {

    }

    @Override
    public void onDispose() {

    }
}
