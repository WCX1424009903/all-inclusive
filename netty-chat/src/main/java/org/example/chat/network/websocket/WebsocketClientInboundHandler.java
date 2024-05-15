package org.example.chat.network.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.example.chat.handler.ServerCoreHandler;
import org.example.chat.network.Gateway;
import org.example.chat.protocal.Protocal;
import org.example.chat.utils.ServerToolKits;

/**
 * WebSocket处理客户端入站事件
 *
 * @author wcx
 * @date 2024/05/10
 */
@Slf4j
public class WebsocketClientInboundHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private ServerCoreHandler serverCoreHandler;

    public WebsocketClientInboundHandler(ServerCoreHandler serverCoreHandler) {
        this.serverCoreHandler = serverCoreHandler;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        try {
            if (e instanceof ReadTimeoutException) {
                log.info("[IMCORE-ws]客户端{}的会话已超时失效，很可能是对方非正常通出或网络故障" +
                        "，即将以会话异常的方式执行关闭流程 ...", ServerToolKits.clientInfoToString(ctx.channel()));
            }
            serverCoreHandler.exceptionCaught(ctx.channel(), e);
        } catch (Exception e2) {
            log.warn(e2.getMessage(), e);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Gateway.setSocketType(ctx.channel(), Gateway.SOCKET_TYPE_WEBSOCKET);
        serverCoreHandler.sessionCreated(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Gateway.removeSocketType(ctx.channel());
        serverCoreHandler.sessionClosed(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String frameContent = ((TextWebSocketFrame) frame).text();
            if (frameContent != null) {
                Protocal pFromClient = ServerToolKits.toProtocal(frameContent);
                serverCoreHandler.messageReceived(ctx.channel(), pFromClient);
            } else {
                throw new UnsupportedOperationException("不支持的 frame content (is null!!)");
            }
        } else {
            throw new UnsupportedOperationException("不支持的 frame type: " + frame.getClass().getName());
        }
    }
}
