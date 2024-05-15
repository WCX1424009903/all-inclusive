
package org.example.chat.processor;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.chat.handler.ServerCoreHandler;
import org.example.chat.network.Gateway;
import org.example.chat.network.MBObserver;
import org.example.chat.protocal.Protocal;
import org.example.chat.protocal.ProtocalFactory;
import org.example.chat.protocal.c.PLoginInfo;
import org.example.chat.utils.LocalSendHelper;

/**
 * 逻辑处理器
 *
 * @author wcx
 * @date 2024/05/11
 */
@Slf4j
public class LogicProcessor {
    private ServerCoreHandler serverCoreHandler;

    public LogicProcessor(ServerCoreHandler serverCoreHandler) {
        this.serverCoreHandler = serverCoreHandler;
    }

    public void processC2SMessage(Channel session, final Protocal pFromClient, String remoteAddress) throws Exception {
        // 服务端收到消息并投递到消息队列
        boolean processedOk = this.serverCoreHandler.getServerEventListener().onTransferMessage4C2S(pFromClient,
                session);
        if (processedOk) {
            log.debug("[IMCORE-{}<C2C>-桥接↑]>> 客户端{}的数据已跨机器送出成功【OK】。(数据[from:{}" +
                            ",fp:{},to:{},content:{}])"
                    , Gateway.getGatewayFlag(session), remoteAddress, pFromClient.getFrom(), pFromClient.getFp()
                    , pFromClient.getTo(), pFromClient.getDataContent());

            // ACK应答包，告诉客户端消息服务端已收到，客户端可以进行本地消息存储
            if (pFromClient.isQoS()) {
                LocalSendHelper.replyRecievedBack(session
                        , pFromClient
                        , (receivedBackSendSucess, extraObj) -> {
                            if (receivedBackSendSucess) {
                                log.debug("[IMCORE-本机QoS！]【QoS_应答_C2S】向" + pFromClient.getFrom() + "发送" + pFromClient.getFp()
                                        + "的应答包成功了,from=" + pFromClient.getTo() + ".");
                                // 消息推送到接收方，单机判断在线处理，集群通过消息队列投递
                                LocalSendHelper.sendData(pFromClient, (sucess, extraPushObj) -> {
                                    if (sucess) {
                                        this.serverCoreHandler.getBridgeProcessor().realtimeC2CSuccessCallback(pFromClient);
                                        log.info("[IMCORE-桥接↓] - " + pFromClient.getFrom() + "发给" + pFromClient.getTo()
                                                + "的指纹为" + pFromClient.getFp() + "的消息转发成功！");
                                    } else {
                                        log.info("[IMCORE-桥接↓]>> 客户端" + pFromClient.getFrom() + "发送给" + pFromClient.getTo() + "的桥接数据尝试实时发送没有成功("
                                                + pFromClient.getTo() + "不在线)，将交给应用层进行离线存储哦...");

                                        boolean offlineProcessedOk =
                                                this.serverCoreHandler.getBridgeProcessor().offlineC2CProcessCallback(pFromClient);
                                        if (offlineProcessedOk) {
                                            log.debug("[IMCORE-桥接↓]>> 向" + pFromClient.getFrom() + "发送" + pFromClient.getFp()
                                                    + "的消息【离线处理】成功,from=" + pFromClient.getTo() + ".");
                                        } else {
                                            log.warn("[IMCORE-桥接↓]>> 客户端" + pFromClient.getFrom() + "发送给" + pFromClient.getTo() + "的桥接数据传输消息尝试实时发送没有成功，但上层应用层没有成" +
                                                    "功(或者完全没有)进行离线存储，此消息将被服务端丢弃！");
                                        }
                                    }
                                });
                            }
                        }
                );
            }
        } else {
            log.debug("[IMCORE-{}<C2C>-桥接↑]>> 客户端{}的数据已跨机器送出失败，将作离线处理了【NO】。(数据[from:{},fp:{},to:{}," +
                            "content:{}])"
                    , Gateway.getGatewayFlag(session), remoteAddress, pFromClient.getFrom(), pFromClient.getFp()
                    , pFromClient.getTo(), pFromClient.getDataContent());
            this.serverCoreHandler.getBridgeProcessor().offlineC2CProcessCallback(pFromClient);
        }
    }

    public void processACK(final Protocal pFromClient, final String remoteAddress) {
        String theFingerPrint = pFromClient.getDataContent();
        log.debug("[IMCORE-本机QoS！]【QoS机制_S2C】收到接收者" + pFromClient.getFrom() + "回过来的指纹为" + theFingerPrint + "的应答包.");
        if (this.serverCoreHandler.getServerMessageQoSEventListener() != null) {
            this.serverCoreHandler.getServerMessageQoSEventListener().messagesBeReceived(theFingerPrint);
        }
    }

    public void processLogin(final Channel session, final Protocal pFromClient, final String remoteAddress) throws Exception {
        final PLoginInfo loginInfo = ProtocalFactory.parsePLoginInfo(pFromClient.getDataContent());

        if (loginInfo == null || loginInfo.getLoginUserId() == null) {
            log.warn("[IMCORE-{}]>> 收到客户端{}登陆信息，但loginInfo或loginInfo.getLoginUserId()是null，登陆无法继续[uid={}、token={}、firstLoginTime={}]！"
                    , Gateway.getGatewayFlag(session), remoteAddress, loginInfo, loginInfo != null ? loginInfo.getLoginUserId() :
                            null, loginInfo != null ? loginInfo.getFirstLoginTime() : null);
            session.close();
            return;
        }

        log.info("[IMCORE-{}]>> 客户端" + remoteAddress + "发过来的登陆信息内容是：uid={}、token={}、firstLoginTime={}"
                , Gateway.getGatewayFlag(session), loginInfo.getLoginUserId(), loginInfo.getLoginToken(), loginInfo.getFirstLoginTime());

        if (serverCoreHandler.getServerEventListener() != null) {
            //boolean alreadyLogined = OnlineProcessor.isLogined(session);
            boolean alreadyLogined = false;
            if (alreadyLogined) {
                log.debug("[IMCORE-{}]>> 【注意】客户端{}的会话正常且已经登陆过，而此时又重新登陆：uid={}、token={}、firstLoginTime={}"
                        , Gateway.getGatewayFlag(session), remoteAddress, loginInfo.getLoginUserId(), loginInfo.getLoginToken(), loginInfo.getFirstLoginTime());
                processLoginSucessSend(session, loginInfo, remoteAddress);
            } else {
                int code = serverCoreHandler.getServerEventListener().onUserLoginVerify(
                        loginInfo.getLoginUserId(), loginInfo.getLoginToken(), loginInfo.getExtra(), session);
                if (code == 0) {
                    processLoginSucessSend(session, loginInfo, remoteAddress);
                } else {
                    log.warn("[IMCORE-{}]>> 客户端{}登陆失败【no】，马上返回失败信息，并关闭其会话。。。", Gateway.getGatewayFlag(session), remoteAddress);

                    MBObserver sendResultObserver = new MBObserver() {
                        @Override
                        public void update(boolean sendOK, Object extraObj) {
                            log.warn("[IMCORE-{}]>> 客户端{}登陆失败信息返回成功？{}（会话即将关闭）", Gateway.getGatewayFlag(session), remoteAddress, sendOK);
                            session.close();
                        }
                    };

                    LocalSendHelper.sendData(session, ProtocalFactory.createPLoginInfoResponse(code, -1, "-1"), sendResultObserver);
                }
            }
        } else {
            log.warn("[IMCORE-{}]>> 收到客户端{}登陆信息，但回调对象是null，没有进行回调.", Gateway.getGatewayFlag(session), remoteAddress);
        }
    }

    private void processLoginSucessSend(final Channel session, final PLoginInfo loginInfo, final String remoteAddress) throws Exception {
        final long firstLoginTimeFromClient = loginInfo.getFirstLoginTime();
        // (firstLoginTimeFromClient <= 0);
        final boolean firstLogin = PLoginInfo.isFirstLogin(firstLoginTimeFromClient);
        final long firstLoginTimeToClient = (firstLogin ? System.currentTimeMillis() : firstLoginTimeFromClient);

        MBObserver sendResultObserver = new MBObserver() {
            @Override
            public void update(boolean __sendOK, Object extraObj) {
                if (__sendOK) {
                    boolean putOk = OnlineProcessor.getInstance().putUser(loginInfo.getLoginUserId(),
                            firstLoginTimeFromClient, session);
                    if (putOk) {
                        OnlineProcessor.setUserIdForChannel(session, loginInfo.getLoginUserId());
                        OnlineProcessor.setFirstLoginTimeForChannel(session, firstLoginTimeToClient);
                        serverCoreHandler.getServerEventListener().onUserLoginSucess(loginInfo.getLoginUserId(), loginInfo.getExtra(), session);
                    }
                } else {
                    log.warn("[IMCORE-{}]>> 发给客户端{}的登陆成功信息发送失败了【no】！", Gateway.getGatewayFlag(session), remoteAddress);
                }
            }
        };
        LocalSendHelper.sendData(session, ProtocalFactory.createPLoginInfoResponse(0, firstLoginTimeToClient, loginInfo.getLoginUserId()), sendResultObserver);
    }

    public void processKeepAlive(Channel session, Protocal pFromClient, String remoteAddress) throws Exception {
        String userId = OnlineProcessor.getUserIdFromChannel(session);
        if (userId != null) {
            LocalSendHelper.sendData(ProtocalFactory.createPKeepAliveResponse(userId), null);
        } else {
            log.warn("[IMCORE-{}]>> Server在回客户端{}的响应包时，调用getUserIdFromSession返回null，用户在这一瞬间掉线了？！", Gateway.getGatewayFlag(session),
                    remoteAddress);
        }
    }
}
