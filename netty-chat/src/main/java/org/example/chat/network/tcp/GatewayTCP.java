
package org.example.chat.network.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.example.chat.handler.ServerCoreHandler;
import org.example.chat.network.Gateway;

/**
 * TCP网关
 *
 * @author wcx
 * @date 2024/05/14
 */
@Slf4j
public class GatewayTCP extends Gateway {
    public static int PORT = 8901;

    public static int SESION_RECYCLER_EXPIRE = 20;

    public static int TCP_FRAME_FIXED_HEADER_LENGTH = 4;     // 4 bytes

    public static int TCP_FRAME_MAX_BODY_LENGTH = 6 * 1024; // 6K bytes

    public static SslContext sslContext = null;

    protected final EventLoopGroup bossGroup4Netty = new NioEventLoopGroup(1);
    protected final EventLoopGroup workerGroup4Netty = new NioEventLoopGroup();
    protected Channel serverChannel4Netty = null;

    protected ServerBootstrap bootstrap = null;

    /**
     * 在Netty中，ChannelOption.SO_BACKLOG 属性是用来配置服务器端的 ServerSocket 对象的一个关键参数。
     * 它的作用是设定已完成三次握手的客户端连接请求在被服务器实际接受并分配到服务线程处理之前，可以在操作系统内核的连接队列中等待的最大数量。
     * 这个队列通常称为已完成连接队列或半连接队列。
     * 当客户端尝试与服务器建立TCP连接时，它们会进行著名的三次握手过程。
     * 一旦握手完成，客户端的连接请求就在服务器端的这个队列中等待，直到服务器有空闲的线程可以处理新的连接。
     * 如果连接请求的数量超过了SO_BACKLOG设置的值，额外的连接请求将被拒绝，客户端通常会收到一个错误信息，比如“暂时无法连接”。
     * 默认情况下，Java的ServerSocket类会将SO_BACKLOG设置为50。如果你预期高并发连接，你可以根据服务器的处理能力适当增加这个值，
     * 以避免因连接队列溢出而导致的连接丢失。
     * 请注意，即使设置了较大的SO_BACKLOG值，也并不意味着服务器可以无限地接受连接。它仍然受限于操作系统的限制，
     * 每个系统都有自己的最大允许值，这个值可以通过操作系统特定的命令查询（例如在Linux上使用ulimit命令）。
     * 因此，设置SO_BACKLOG时应考虑到这些限制。
     */

    /**
     * 在Netty中，ChannelOption.TCP_NODELAY 属性对应于TCP协议中的 Nagle 算法的禁用。
     * Nagle算法是一种优化网络传输效率的机制，它旨在减少小数据包的发送，通过合并多个小的数据片段为一个大的数据包来减少网络拥堵。
     * 然而，这可能会导致一定的延迟，因为数据不会立即发送，而是等待更多的数据积累或达到一个阈值后才一起发送。
     * TCP_NODELAY 设置为 true 表示禁用 Nagle 算法，这意味着每个数据段都会尽快单独发送，而不是等待积累。
     * 这通常用于那些需要低延迟、高实时性的应用，如在线游戏、实时交易系统或者任何需要即时反馈的交互式应用。
     * 虽然这可能会增加网络上的小包流量，但它确保了数据的快速传输。
     * 相反，如果TCP_NODELAY 设置为 false 或不设置，Nagle算法默认启用，这可能有助于提高带宽效率，但可能会增加数据传输的延迟，尤其是对于小数据量的传输。
     */

    @Override
    public void init(ServerCoreHandler serverCoreHandler) {
        bootstrap = new ServerBootstrap()
                .group(bossGroup4Netty, workerGroup4Netty)
                .channel(NioServerSocketChannel.class)
                .childHandler(initChildChannelHandler(serverCoreHandler));
        bootstrap.option(ChannelOption.SO_BACKLOG, 4096);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
    }

    @Override
    public void bind() throws Exception {
        ChannelFuture cf = bootstrap.bind(PORT).sync();
        if (cf.isSuccess()) {
            log.info("[IMCORE-tcp] TCP服务绑定端口" + PORT + "成功 √ " + (isSsl() ? "(已开启SSL/TLS加密传输)" : ""));
        } else {
            log.info("[IMCORE-tcp] TCP服务绑定端口" + PORT + "失败 ×");
        }

        serverChannel4Netty = cf.channel();
        serverChannel4Netty.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                bossGroup4Netty.shutdownGracefully();
                workerGroup4Netty.shutdownGracefully();
            }
        });

        log.info("[IMCORE-tcp] .... continue ...");
        log.info("[IMCORE-tcp] TCP服务正在端口" + PORT + "上监听中...");
    }

    @Override
    public void shutdown() {
        if (serverChannel4Netty != null) {
            serverChannel4Netty.close();
        }
    }

    protected ChannelHandler initChildChannelHandler(final ServerCoreHandler serverCoreHandler) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {

                ChannelPipeline pipeline = channel.pipeline();

                if (sslContext != null) {
                    pipeline.addFirst(sslContext.newHandler(channel.alloc()));
                }

                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(
                        TCP_FRAME_FIXED_HEADER_LENGTH + TCP_FRAME_MAX_BODY_LENGTH
                        , 0, TCP_FRAME_FIXED_HEADER_LENGTH, 0, TCP_FRAME_FIXED_HEADER_LENGTH));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(TCP_FRAME_FIXED_HEADER_LENGTH));
                pipeline.addLast(new ReadTimeoutHandler(SESION_RECYCLER_EXPIRE));
                pipeline.addLast(new TCPClientInboundHandler(serverCoreHandler));
            }
        };
    }

    public static boolean isSsl() {
        return sslContext != null;
    }
}
