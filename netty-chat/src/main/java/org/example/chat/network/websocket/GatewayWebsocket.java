package org.example.chat.network.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.chat.handler.ServerCoreHandler;
import org.example.chat.network.Gateway;

/**
 * 网关 WebSocket
 *
 * @author wcx
 * @date 2024/05/10
 */
@Slf4j
public class GatewayWebsocket extends Gateway {
    /**
     * websocket前缀路径
     */
    public static String WEBSOCKET_PATH = "/websocket";
    /**
     * 是否开启ssl
     */
    public static boolean SSL = false;
    /**
     * websocket端口
     */
    public static int PORT = 3000;
    /**
     * 会话回收器过期时间
     */
    public static int SESION_RECYCLER_EXPIRE = 20;

    public static SslContext sslContext = null;
    /**
     * boss线程组
     */
    protected final EventLoopGroup bossGroupWebSocket = new NioEventLoopGroup(1);
    /**
     * worker线程组
     */
    protected final EventLoopGroup workerGroupWebSocket = new NioEventLoopGroup();
    /**
     * channel通道
     */
    protected Channel serverChannelWebSocket = null;
    /**
     * 服务引导器
     */
    protected ServerBootstrap bootstrap = null;

    @Override
    public void init(ServerCoreHandler serverCoreHandler) {
        SslContext sslCtx = null;
        try {
            if (SSL) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                // ssl证书可以从外部读取，传入InputStream
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            } else {
                sslCtx = null;
            }
        } catch (Exception e) {
            log.error("websocket中SSL证书准备失败：", e);
        }
        bootstrap = new ServerBootstrap()
                .group(bossGroupWebSocket, workerGroupWebSocket)
                .channel(NioServerSocketChannel.class)
                .childHandler(initChildChannelHandler(serverCoreHandler));
    }

    @Override
    public void bind() throws Exception {
        ChannelFuture cf = bootstrap.bind(PORT).sync();
        if (cf.isSuccess()) {
            log.info("[IMCORE-ws] WebSocket服务绑定端口" + PORT + "成功 √ " + (isSsl() ? "(已开启SSL/TLS加密传输)" : ""));
        } else {
            log.info("[IMCORE-ws] WebSocket服务绑定端口" + PORT + "失败 ×");
        }

        serverChannelWebSocket = cf.channel();
        serverChannelWebSocket.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                bossGroupWebSocket.shutdownGracefully();
                workerGroupWebSocket.shutdownGracefully();
            }
        });
        log.info("[IMCORE-ws] .... continue ...");
        log.info("[IMCORE-ws] WebSocket服务正在端口" + PORT + "上监听中" + (SSL ? "(已开启SSL)" : "") + "...");
    }

    @Override
    public void shutdown() {
        if (serverChannelWebSocket != null) {
            serverChannelWebSocket.close();
        }
    }

    protected ChannelHandler initChildChannelHandler(final ServerCoreHandler serverCoreHandler) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                ChannelPipeline pipeline = channel.pipeline();

                if (sslContext != null) {
                    pipeline.addLast(sslContext.newHandler(channel.alloc()));
                }

                // HttpRequestDecoder和HttpResponseEncoder的一个组合，针对http协议进行编解码
                pipeline.addLast(new HttpServerCodec());
                // 将HttpMessage和HttpContents聚合到一个完成的 FullHttpRequest或FullHttpResponse中,具体是FullHttpRequest对象还是FullHttpResponse对象取决于是请求还是响应
                // 需要放到HttpServerCodec这个处理器后面
                pipeline.addLast(new HttpObjectAggregator(65536));
                // 分块向客户端写数据，防止发送大文件时导致内存溢出， channel.write(new ChunkedFile(new File("bigFile.mkv")))
                //pipeline.addLast(new ChunkedWriteHandler());
                // 服务器端向外暴露的websocket 端点，当客户端传递比较大的对象时，maxFrameSize参数的值需要调大
                pipeline.addLast(new WebSocketServerProtocolHandler(WEBSOCKET_PATH, null, true));
                // 心跳检测处理
                pipeline.addLast(new ReadTimeoutHandler(SESION_RECYCLER_EXPIRE));
                pipeline.addLast(new WebsocketClientInboundHandler(serverCoreHandler));
            }
        };
    }

    /**
     * 是否开启SSL
     *
     * @return
     */
    public static boolean isSsl() {
        return sslContext != null;
    }

}
