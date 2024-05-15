
package org.example.chat.handler;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.chat.event.MessageQoSEventListenerS2C;
import org.example.chat.event.ServerEventListener;
import org.example.chat.network.Gateway;
import org.example.chat.processor.BridgeProcessor;
import org.example.chat.processor.LogicProcessor;
import org.example.chat.protocal.Protocal;
import org.example.chat.protocal.ProtocalType;
import org.example.chat.utils.LocalSendHelper;
import org.example.chat.processor.OnlineProcessor;
import org.example.chat.utils.ServerToolKits;

/**
 * 服务器核心处理程序
 *
 * @author wcx
 * @date 2024/05/11
 */
@Slf4j
public class ServerCoreHandler {
    protected ServerEventListener serverEventListener = null;

    protected MessageQoSEventListenerS2C serverMessageQoSEventListener = null;

    protected LogicProcessor logicProcessor = null;

    protected BridgeProcessor bridgeProcessor = null;

    public ServerCoreHandler() {
        logicProcessor = this.createLogicProcessor();

        bridgeProcessor = this.createBridgeProcessor();
    }

    protected LogicProcessor createLogicProcessor() {
        return new LogicProcessor(this);
    }

    protected BridgeProcessor createBridgeProcessor() {
        BridgeProcessor bp = new BridgeProcessor() {
            protected void realtimeC2CSuccessCallback(Protocal p) {
                serverEventListener.onTransferMessage4C2C(p);
            }

            @Override
            protected boolean offlineC2CProcessCallback(Protocal p) {
                return serverEventListener.onTransferMessage_RealTimeSendFaild(p);
            }
        };
        return bp;
    }

    /**
     * 连接异常处理
     */
    public void exceptionCaught(Channel session, Throwable cause) {
        log.debug("[IMCORE-" + Gateway.getGatewayFlag(session) + "]此客户端的Channel抛出了exceptionCaught，原因是："
                + cause.getMessage() + "，可以提前close掉了哦！", cause);
        session.close();
    }

    /**
     * 收到消息处理，进行自定义协议体内容解析，做出处理
     *
     * @param session     会话
     * @param pFromClient 发送者消息处理
     * @throws Exception
     */
    public void messageReceived(Channel session, Protocal pFromClient) throws Exception {
        // 发送方远程连接地址
        String remoteAddress = ServerToolKits.clientInfoToString(session);

        switch (pFromClient.getType()) {
            /** 由客户端发出 - 协议类型：QoS保证机制中的消息应答包（目前只支持客户端间的QoS机制哦） */
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_RECIVED: {
                log.info("[IMCORE-{}]<< 收到客户端{}的ACK应答包发送请求.", Gateway.getGatewayFlag(session), remoteAddress);
                if (!OnlineProcessor.isLogined(session)) {
                    LocalSendHelper.replyDataForUnlogined(session, pFromClient, null);
                    return;
                }
                logicProcessor.processACK(pFromClient, remoteAddress);
                break;
            }
            /** 由客户端发出 - 协议类型：发送通用聊天数据 */
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_COMMON$DATA: {
                log.info("[IMCORE-{}]<< 收到客户端{}的通用数据发送请求.", Gateway.getGatewayFlag(session), remoteAddress);

                if (serverEventListener != null) {
                    if (!OnlineProcessor.isLogined(session)) {
                        LocalSendHelper.replyDataForUnlogined(session, pFromClient, null);
                        return;
                    }
                    // 消息前置处理
                    if (serverEventListener.onTransferMessage4C2CBefore(pFromClient, session)) {
                        logicProcessor.processC2SMessage(session, pFromClient, remoteAddress);
                    }
                } else {
                    log.warn("[IMCORE-{}]<< 收到客户端{}的通用数据传输消息，但回调对象是null，回调无法继续.", Gateway.getGatewayFlag(session), remoteAddress);
                }
                break;
            }
            /**客户端心跳包检测，服务端响应*/
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_KEEP$ALIVE: {
                if (!OnlineProcessor.isLogined(session)) {
                    LocalSendHelper.replyDataForUnlogined(session, pFromClient, null);
                    break;
                } else {
                    logicProcessor.processKeepAlive(session, pFromClient, remoteAddress);
                }
                break;
            }
            /** 由客户端发出 - 协议类型：客户端登陆 */
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGIN: {
                logicProcessor.processLogin(session, pFromClient, remoteAddress);
                break;
            }
            /** 由客户端发出 - 协议类型：客户端退出登陆 */
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_LOGOUT: {
                log.info("[IMCORE-{}]<< 收到客户端{}的退出登陆请求.", Gateway.getGatewayFlag(session), remoteAddress);
                session.close();
                break;
            }
            case ProtocalType.C.FROM_CLIENT_TYPE_OF_ECHO: {
                pFromClient.setType(ProtocalType.S.FROM_SERVER_TYPE_OF_RESPONSE$ECHO);
                LocalSendHelper.sendData(session, pFromClient, null);
                break;
            }
            default: {
                log.warn("[IMCORE-{}]【注意】收到的客户端{}消息类型：{}，但目前该类型服务端不支持解析和处理！"
                        , Gateway.getGatewayFlag(session), remoteAddress, pFromClient.getType());
                break;
            }
        }
    }

    public void sessionClosed(Channel session) throws Exception {
        String user_id = OnlineProcessor.getUserIdFromChannel(session);

        if (user_id != null) {
            Channel sessionInOnlinelist = OnlineProcessor.getInstance().getOnlineSession(user_id);

            log.info("[IMCORE-{}]{}的会话已关闭(user_id={}, firstLoginTime={})了..."
                    , Gateway.getGatewayFlag(session), ServerToolKits.clientInfoToString(session), user_id,
                    OnlineProcessor.getFirstLoginTimeFromChannel(session));

            //## Bug FIX: 20171211 START
            if (session == sessionInOnlinelist)
            //## Bug FIX: 20171211 END
            {
                int beKickoutCode = OnlineProcessor.getBeKickoutCodeFromChannel(session);

                OnlineProcessor.removeAttributesForChannel(session);
                OnlineProcessor.getInstance().removeUser(user_id);

                if (serverEventListener != null) {
                    serverEventListener.onUserLogout(user_id, session, beKickoutCode);
                } else {
                    log.debug("[IMCORE-{}]>> 会话{}被系统close了，但回调对象是null，没有进行回调通知."
                            , Gateway.getGatewayFlag(session), ServerToolKits.clientInfoToString(session));
                }
            } else {
                log.warn("[IMCORE-{}]【2】【注意】会话{}不在在线列表中，意味着它是被客户端弃用/或被服务端强踢，本次忽略这条关闭事件即可！"
                        , Gateway.getGatewayFlag(session), ServerToolKits.clientInfoToString(session));
            }
        } else {
            log.warn("[IMCORE-{}]【注意】会话{}被系统close了，但它里面没有存放user_id，它很可能是没有成功合法认证而被提前关闭，从而正常释放资源。"
                    , Gateway.getGatewayFlag(session), ServerToolKits.clientInfoToString(session));
        }
    }

    public void sessionCreated(Channel session) throws Exception {
        log.info("[IMCORE-{}]与{}的会话建立(channelActive)了...", Gateway.getGatewayFlag(session),
                ServerToolKits.clientInfoToString(session));
    }

    public ServerEventListener getServerEventListener() {
        return serverEventListener;
    }

    public void setServerEventListener(ServerEventListener serverEventListener) {
        this.serverEventListener = serverEventListener;
    }

    public MessageQoSEventListenerS2C getServerMessageQoSEventListener() {
        return serverMessageQoSEventListener;
    }

    public void setServerMessageQoSEventListener(MessageQoSEventListenerS2C serverMessageQoSEventListener) {
        this.serverMessageQoSEventListener = serverMessageQoSEventListener;
    }

    public BridgeProcessor getBridgeProcessor() {
        return bridgeProcessor;
    }
}
