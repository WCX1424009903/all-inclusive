
package org.example.chat.network.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.example.chat.handler.ServerCoreHandler;
import org.example.chat.network.Gateway;
import org.example.chat.protocal.Protocal;
import org.example.chat.utils.ServerToolKits;

/**
 * tcpclient入站处理程序
 *
 * @author wcx
 * @date 2024/05/14
 */
@Slf4j
public class TCPClientInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private ServerCoreHandler serverCoreHandler = null;

    public TCPClientInboundHandler(ServerCoreHandler serverCoreHandler) {
        this.serverCoreHandler = serverCoreHandler;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
        try {
            if (e instanceof ReadTimeoutException) {
                log.info("[IMCORE-tcp]客户端{}的会话已超时失效，很可能是对方非正常通出或网络故障" +
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
        Gateway.setSocketType(ctx.channel(), Gateway.SOCKET_TYPE_TCP);
        serverCoreHandler.sessionCreated(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Gateway.removeSocketType(ctx.channel());
        serverCoreHandler.sessionClosed(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf bytebuf) throws Exception {
        Protocal pFromClient = ServerToolKits.fromIOBuffer(bytebuf);
        serverCoreHandler.messageReceived(ctx.channel(), pFromClient);
    }
}