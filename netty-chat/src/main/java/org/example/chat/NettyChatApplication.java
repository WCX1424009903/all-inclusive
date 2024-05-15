package org.example.chat;

import org.example.chat.logicimpl.ServerLauncherAchieve;
import org.example.chat.network.Gateway;

public class NettyChatApplication {

    public static void main(String[] args) throws Exception {
        ServerLauncherAchieve serverLauncherAchieve = new ServerLauncherAchieve();
        ServerLauncherAchieve.serverTimestamp = true;
        // 同时支持两种类型启动，进行位或运算
        ServerLauncherAchieve.supportedGateways = Gateway.SOCKET_TYPE_TCP | Gateway.SOCKET_TYPE_WEBSOCKET;
        serverLauncherAchieve.startup();
        // 加一个钩子，确保在JVM退出时释放netty的资源
        Runtime.getRuntime().addShutdownHook(new Thread(serverLauncherAchieve::shutdown));
    }

}
