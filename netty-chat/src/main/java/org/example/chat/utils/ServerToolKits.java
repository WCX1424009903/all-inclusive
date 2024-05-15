
package org.example.chat.utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import org.example.chat.network.websocket.GatewayWebsocket;
import org.example.chat.processor.OnlineProcessor;
import org.example.chat.protocal.CharsetHelper;
import org.example.chat.protocal.Protocal;
import org.example.chat.protocal.ProtocalFactory;

import java.net.SocketAddress;

/**
 * 服务器工具包
 *
 * @author wcx
 * @date 2024/05/10
 */
public class ServerToolKits {
    public static void setSenseModeWebsocket(SenseModeWebsocket mode) {
        int expire = 0;
        switch (mode) {
            case MODE_3S:
                expire = 3 * 2 + 1;
                break;
            case MODE_5S:
                expire = 5 * 2 + 1;
                break;
            case MODE_10S:
                expire = 10 * 1 + 5;
                break;
            case MODE_15S:
                expire = 15 * 1 + 5;
                break;
            case MODE_30S:
                expire = 30 * 1 + 5;
                break;
            case MODE_60S:
                expire = 60 * 1 + 5;
                break;
            case MODE_120S:
                expire = 120 * 1 + 5;
                break;
        }

        if (expire > 0)
            GatewayWebsocket.SESION_RECYCLER_EXPIRE = expire;
    }

    public static String clientInfoToString(Channel session) {
        SocketAddress remoteAddress = session.remoteAddress();
        String s1 = remoteAddress.toString();
        StringBuilder sb = new StringBuilder()
                .append("{uid:")
                .append(OnlineProcessor.getUserIdFromChannel(session))
                .append("}")
                .append(s1);
        return sb.toString();
    }

    public static String fromIOBuffer_JSON(ByteBuf buffer) throws Exception {
        byte[] req = new byte[buffer.readableBytes()];
        buffer.readBytes(req);
        String jsonStr = new String(req, CharsetHelper.DECODE_CHARSET);
        return jsonStr;
    }

    public static Protocal fromIOBuffer(ByteBuf buffer) throws Exception {
        return toProtocal(fromIOBuffer_JSON(buffer));
    }

    public static Protocal toProtocal(String protocalJSONStr) throws Exception {
        return ProtocalFactory.parse(protocalJSONStr, Protocal.class);
    }

    /**
     * MobileIMSDK核心框架的TCP协议心跳频率模式.
     * <p>
     * 对于服务端而言，此模式决定了用户在非正常退出、心跳丢包、网络故障等情况下
     * 被判定为已下线的超时时长，原则上超敏感客户端的体验越好。
     * <p>
     * <b>重要说明：</b><u>服务端本模式的设定必须要与客户端的模式设制保持一致</u>，否则
     * 可能因参数的不一致而导至IM算法的不匹配，进而出现不可预知的问题。
     *
     * @author Jack Jiang
     * @version 5.0
     */
    public enum SenseModeTCP {
        /**
         * 对应于客户端的3秒心跳模式：此模式的用户非正常掉线超时时长为“3 * 2 + 1”秒
         * （即：<b>非正常连接超时时间为2个心跳包间隔+1秒容忍时间</b>）。
         * <p>
         * 此模式为当前所有预设模式中体验最好，但客户端可能会大幅提升耗电量和心跳包的总流量。
         */
        MODE_3S,

        /**
         * 对应于客户端的5秒心跳模式：此模式的用户非正常掉线超时时长为“5 * 2 + 1”秒
         * （即：<b>非正常连接超时时间为2个心跳包间隔+1秒容忍时间</b>）。
         * <p>
         * 此模式为当前所有预设模式中体验稍好，但客户端可能会较多提升耗电量和心跳包的总流量。
         */
        MODE_5S,

        /**
         * 对应于客户端的10秒心跳模式：此模式的用户非正常掉线超时时长为“10 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒容忍时间）。
         */
        MODE_10S,

        /**
         * 对应于客户端的15秒心跳模式：此模式的用户非正常掉线超时时长为“15 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒容忍时间）。
         */
        MODE_15S,

        /**
         * 对应于客户端的30秒心跳模式：此模式的用户非正常掉线超时时长为“30 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒容忍时间）。
         */
        MODE_30S,

        /**
         * 对应于客户端的60秒心跳模式：此模式的用户非正常掉线超时时长为“60 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒容忍时间）。
         */
        MODE_60S,

        /**
         * 对应于客户端的120秒心跳模式：此模式的用户非正常掉线超时时长为“120 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒容忍时间）。
         */
        MODE_120S
    }

    /**
     * MobileIMSDK核心框架的UDP协议心跳频率模式.
     * <p>
     * 对于服务端而言，此模式决定了用户在非正常退出、心跳丢包、网络故障等情况下
     * 被判定为已下线的超时时长，原则上超敏感客户端的体验越好。
     * <p>
     * <b>重要说明：</b><u>服务端本模式的设定必须要与客户端的模式设制保持一致</u>，否则
     * 可能因参数的不一致而导至IM算法的不匹配，进而出现不可预知的问题。
     *
     * @author Jack Jiang
     * @version 2.1
     */
    public enum SenseModeUDP {
        /**
         * 对应于客户端的3秒心跳模式：此模式的用户非正常掉线超时时长为“3 * 3 + 1”秒。
         * <p>
         * 客户端心跳丢包容忍度为3个包。此模式为当前所有预设模式中体验最好，但
         * 客户端可能会大幅提升耗电量和心跳包的总流量。
         */
        MODE_3S,

        /**
         * 对应于客户端的10秒心跳模式：此模式的用户非正常掉线超时时长为“10 * 2 + 1”秒。
         * <p>
         * 客户端心跳丢包容忍度为2个包。
         */
        MODE_10S,

        /**
         * 对应于客户端的30秒心跳模式：此模式的用户非正常掉线超时时长为“30 * 2 + 2”秒。
         * <p>
         * 客户端心跳丢包容忍度为2个包。
         */
        MODE_30S,

        /**
         * 对应于客户端的60秒心跳模式：此模式的用户非正常掉线超时时长为“60 * 2 + 2”秒。
         * <p>
         * 客户端心跳丢包容忍度为2个包。
         */
        MODE_60S,

        /**
         * 对应于客户端的120秒心跳模式：此模式的用户非正常掉线超时时长为“120 * 2 + 2”秒。
         * <p>
         * 客户端心跳丢包容忍度为2个包。
         */
        MODE_120S
    }

    /**
     * MobileIMSDK核心框架的WebSocket协议心跳频率模式.
     * <p>
     * 对于服务端而言，此模式决定了用户在非正常退出、心跳丢包、网络故障等情况下
     * 被判定为已下线的超时时长，原则上超敏感客户端的体验越好。
     * <p>
     * <b>重要说明：</b><u>服务端本模式的设定必须要与客户端的模式设制保持一致</u>，否则
     * 可能因参数的不一致而导至IM算法的不匹配，进而出现不可预知的问题。
     *
     * @author Jack Jiang
     * @version 6.0
     */
    public enum SenseModeWebsocket {
        /**
         * 对应于客户端的3秒心跳模式：此模式的用户非正常掉线超时时长为“3 * 2 + 1”秒
         * （即：<b>非正常连接超时时间为2个心跳包间隔+1秒链路延迟容忍时间</b>）。
         * <p>
         * 此模式为当前所有预设模式中体验最好，但客户端可能会大幅提升耗电量和心跳包的总流量。
         */
        MODE_3S,
        /**
         * 对应于客户端的5秒心跳模式：此模式的用户非正常掉线超时时长为“5 * 2 + 1”秒
         * （即：<b>非正常连接超时时间为2个心跳包间隔+1秒容忍时间</b>）。
         * <p>
         * 此模式为当前所有预设模式中体验稍好，但客户端可能会较多提升耗电量和心跳包的总流量。
         */
        MODE_5S,

        /**
         * 对应于客户端的10秒心跳模式：此模式的用户非正常掉线超时时长为“10 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒链路延迟容忍时间）。
         */
        MODE_10S,

        /**
         * 对应于客户端的15秒心跳模式：此模式的用户非正常掉线超时时长为“15 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒链路延迟容忍时间）。
         */
        MODE_15S,

        /**
         * 对应于客户端的30秒心跳模式：此模式的用户非正常掉线超时时长为“30 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒链路延迟容忍时间）。
         */
        MODE_30S,

        /**
         * 对应于客户端的60秒心跳模式：此模式的用户非正常掉线超时时长为“60 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒链路延迟容忍时间）。
         */
        MODE_60S,

        /**
         * 对应于客户端的120秒心跳模式：此模式的用户非正常掉线超时时长为“120 * 1 + 5”秒
         * （即：非正常连接超时时间为 1个心跳包间隔+5秒链路延迟容忍时间）。
         */
        MODE_120S
    }
}
