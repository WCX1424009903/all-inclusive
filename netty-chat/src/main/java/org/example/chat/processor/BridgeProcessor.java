package org.example.chat.processor;

import org.example.chat.protocal.Protocal;

public abstract class BridgeProcessor {

    /**
     * 在线消息回调
     */
    protected abstract void realtimeC2CSuccessCallback(Protocal p);

    /**
     * 离线消息回调
     */
    protected abstract boolean offlineC2CProcessCallback(Protocal p);

}
