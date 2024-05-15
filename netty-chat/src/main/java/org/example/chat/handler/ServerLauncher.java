
package org.example.chat.handler;

import lombok.extern.slf4j.Slf4j;
import org.example.chat.event.MessageQoSEventListenerS2C;
import org.example.chat.event.ServerEventListener;
import org.example.chat.network.Gateway;
import org.example.chat.network.tcp.GatewayTCP;
import org.example.chat.network.websocket.GatewayWebsocket;

import java.io.IOException;

/**
 * 服务器启动器
 *
 * @author wcx
 * @date 2024/05/14
 */
@Slf4j
public abstract class ServerLauncher {

    public static boolean serverTimestamp = false;

    public static int supportedGateways = 0;

    protected ServerCoreHandler serverCoreHandler = null;

    private boolean running = false;

    private Gateway tcp = null;
    private Gateway ws = null;

    public ServerLauncher() throws IOException {
        // default do nothing
    }

    protected ServerCoreHandler initServerCoreHandler() {
        return new ServerCoreHandler();
    }

    protected abstract void initListeners();

    protected void initGateways() {
        if (Gateway.isSupportTCP(supportedGateways)) {
            tcp = createGatewayTCP();
            tcp.init(this.serverCoreHandler);
        }

        if (Gateway.isSupportWebSocket(supportedGateways)) {
            ws = createGatewayWebsocket();
            ws.init(this.serverCoreHandler);
        }
    }

    protected GatewayTCP createGatewayTCP() {
        return new GatewayTCP();
    }

    protected GatewayWebsocket createGatewayWebsocket() {
        return new GatewayWebsocket();
    }

    public void startup() throws Exception {
        if (!this.running) {
            serverCoreHandler = initServerCoreHandler();
            initListeners();
            initGateways();

            bind();
            this.running = true;
        } else {
            log.warn("[IMCORE] 通信服务正在运行中，本次startup()失败，请先调用shutdown()后再试！");
        }
    }

    protected void bind() throws Exception {
        if (tcp != null) {
            tcp.bind();
        }
        if (ws != null) {
            ws.bind();
        }
    }

    public void shutdown() {
        if (tcp != null) {
            tcp.shutdown();
        }
        if (ws != null) {
            ws.shutdown();
        }
        this.running = false;
    }

    public ServerEventListener getServerEventListener() {
        return serverCoreHandler.getServerEventListener();
    }

    public void setServerEventListener(ServerEventListener serverEventListener) {
        this.serverCoreHandler.setServerEventListener(serverEventListener);
    }

    public MessageQoSEventListenerS2C getServerMessageQoSEventListener() {
        return serverCoreHandler.getServerMessageQoSEventListener();
    }

    public void setServerMessageQoSEventListener(MessageQoSEventListenerS2C serverMessageQoSEventListener) {
        this.serverCoreHandler.setServerMessageQoSEventListener(serverMessageQoSEventListener);
    }

    public ServerCoreHandler getServerCoreHandler() {
        return serverCoreHandler;
    }

    public boolean isRunning() {
        return running;
    }

}
