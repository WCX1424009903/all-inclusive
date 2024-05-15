
package org.example.chat.network;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.example.chat.handler.ServerCoreHandler;

/**
 * 基础网关，定义通用模板，由具体通信协议实现
 *
 * @author wcx
 * @date 2024/05/10
 */
public abstract class Gateway {
    public final static String SOCKET_TYPE_IN_CHANNEL_ATTRIBUTE = "__socket_type__";
    /**
     * 与channel搭配使用，用于判断当前连接的socket类型
     */
    public static final AttributeKey<Integer> SOCKET_TYPE_IN_CHANNEL_ATTRIBUTE_ATTR = AttributeKey.newInstance(SOCKET_TYPE_IN_CHANNEL_ATTRIBUTE);

    public static final int SOCKET_TYPE_UDP = 0x0001;
    public static final int SOCKET_TYPE_TCP = 0x0002;
    public static final int SOCKET_TYPE_WEBSOCKET = 0x0004;

    /**
     * 初始化网关
     *
     * @param serverCoreHandler
     */
    public abstract void init(ServerCoreHandler serverCoreHandler);

    /**
     * 绑定具体端口
     *
     * @throws Exception
     */
    public abstract void bind() throws Exception;

    /**
     * 关闭网关连接
     */
    public abstract void shutdown();

    /**
     * 设置socket类型
     *
     * @param c          通道
     * @param socketType 通道类型
     */
    public static void setSocketType(Channel c, int socketType) {
        c.attr(SOCKET_TYPE_IN_CHANNEL_ATTRIBUTE_ATTR).set(socketType);
    }

    /**
     * 移除当前通道类型
     *
     * @param c channel通道
     */
    public static void removeSocketType(Channel c) {
        c.attr(SOCKET_TYPE_IN_CHANNEL_ATTRIBUTE_ATTR).set(null);
    }

    /**
     * 根据channel获取当前socket类型
     *
     * @param c channel通道
     * @return
     */
    public static int getSocketType(Channel c) {
        Integer socketType = c.attr(SOCKET_TYPE_IN_CHANNEL_ATTRIBUTE_ATTR).get();
        if (socketType != null) {
            return socketType;
        }
        return -1;
    }

    /**
     * 是否支持UDP
     *
     * @param support
     * @return
     */
    public static boolean isSupportUDP(int support) {
        // 位运算
        return (support & SOCKET_TYPE_UDP) == SOCKET_TYPE_UDP;
    }

    /**
     * 是否支持TCP
     *
     * @param support
     * @return
     */
    public static boolean isSupportTCP(int support) {
        // 位运算
        return (support & SOCKET_TYPE_TCP) == SOCKET_TYPE_TCP;
    }

    /**
     * 是否支持WebSocket
     *
     * @param support
     * @return
     */
    public static boolean isSupportWebSocket(int support) {
        // 位运算
        return (support & SOCKET_TYPE_WEBSOCKET) == SOCKET_TYPE_WEBSOCKET;
    }

    /**
     * 是否是TCP通道
     *
     * @param c channel通道
     * @return
     */
    public static boolean isTCPChannel(Channel c) {
        return (c != null && getSocketType(c) == SOCKET_TYPE_TCP);
    }

    /**
     * 是否是UDP通道
     *
     * @param c channel通道
     * @return
     */
    public static boolean isUDPChannel(Channel c) {
        return (c != null && getSocketType(c) == SOCKET_TYPE_UDP);
    }

    /**
     * 是否是WebSocket通道
     *
     * @param c channel通道
     * @return
     */
    public static boolean isWebSocketChannel(Channel c) {
        return (c != null && getSocketType(c) == SOCKET_TYPE_WEBSOCKET);
    }

    /**
     * 获取channel类型标识
     *
     * @param c channel通道
     * @return
     */
    public static String getGatewayFlag(Channel c) {
        if (Gateway.isUDPChannel(c)) {
            return "udp";
        } else if (Gateway.isTCPChannel(c)) {
            return "tcp";
        } else if (Gateway.isWebSocketChannel(c)) {
            return "websocket";
        }
        return "unknow";
    }
}
