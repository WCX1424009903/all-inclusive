
package org.example.chat.protocal;

/**
 * 协议类型
 * 这些协议类型由框架算法决定其意义和用途，不建议用户自行使用，用户自定义协议类型请参见 {@link Protocal} 类中的 typeu 字段。
 */
public interface ProtocalType {
    //------------------------------------------------------- from client
    interface C {
        /**
         * 由客户端发出 - 协议类型：客户端登陆
         */
        int FROM_CLIENT_TYPE_OF_LOGIN = 0;
        /**
         * 由客户端发出 - 协议类型：心跳包
         */
        int FROM_CLIENT_TYPE_OF_KEEP$ALIVE = 1;
        /**
         * 由客户端发出 - 协议类型：发送通用数据
         */
        int FROM_CLIENT_TYPE_OF_COMMON$DATA = 2;
        /**
         * 由客户端发出 - 协议类型：客户端退出登陆
         */
        int FROM_CLIENT_TYPE_OF_LOGOUT = 3;

        /**
         * 由客户端发出 - 协议类型：QoS保证机制中的消息应答包（目前只支持客户端间的QoS机制哦）
         */
        int FROM_CLIENT_TYPE_OF_RECIVED = 4;

        /**
         * 由客户端发出 - 协议类型：C2S时的回显指令（此指令目前仅用于测试时）
         */
        int FROM_CLIENT_TYPE_OF_ECHO = 5;
    }

    //------------------------------------------------------- from server
    interface S {
        /**
         * 由服务端发出 - 协议类型：响应客户端的登陆
         */
        int FROM_SERVER_TYPE_OF_RESPONSE$LOGIN = 50;
        /**
         * 由服务端发出 - 协议类型：响应客户端的心跳包
         */
        int FROM_SERVER_TYPE_OF_RESPONSE$KEEP$ALIVE = 51;

        /**
         * 由服务端发出 - 协议类型：反馈给客户端的错误信息
         */
        int FROM_SERVER_TYPE_OF_RESPONSE$FOR$ERROR = 52;

        /**
         * 由服务端发出 - 协议类型：反馈回显指令给客户端
         */
        int FROM_SERVER_TYPE_OF_RESPONSE$ECHO = 53;

        /**
         * 由服务端发出 - 协议类型：向客户端发出“被踢”指令
         */
        int FROM_SERVER_TYPE_OF_KICKOUT = 54;
    }
}
