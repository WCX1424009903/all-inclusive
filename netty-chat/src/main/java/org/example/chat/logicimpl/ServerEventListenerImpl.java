package org.example.chat.logicimpl;

import io.netty.channel.Channel;
import org.example.chat.event.ServerEventListener;
import org.example.chat.protocal.Protocal;

/**
 * 服务器事件侦听器示例实现
 *
 * @author wcx
 * @date 2024/05/13
 */
public class ServerEventListenerImpl implements ServerEventListener {

    @Override
    public int onUserLoginVerify(String userId, String token, String extra, Channel session) {
        return 0;
    }

    @Override
    public void onUserLoginSucess(String userId, String extra, Channel session) {
        System.out.println("登录成功事件回调成功");
    }

    @Override
    public void onUserLogout(String userId, Channel session, int beKickoutCode) {
        System.out.println("用户退出事件回调成功");
    }

    @Override
    public boolean onTransferMessage4C2CBefore(Protocal p, Channel session) {
        return true;
    }

    @Override
    public boolean onTransferMessage4C2S(Protocal p, Channel session) {
        return true;
    }

    @Override
    public void onTransferMessage4C2C(Protocal p) {
        System.out.println("接收方在线并成功收到消息回调");
    }

    @Override
    public boolean onTransferMessage_RealTimeSendFaild(Protocal p) {
        System.out.println("接收方离线并成功收到消息回调");
        return true;
    }
}
