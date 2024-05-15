package org.example.chat.logicimpl;

import org.example.chat.handler.ServerLauncher;

import java.io.IOException;

/**
 * 服务器启动器逻辑示例实现
 *
 * @author wcx
 * @date 2024/05/14
 */
public class ServerLauncherAchieve extends ServerLauncher {

    public ServerLauncherAchieve() throws IOException {

    }

    @Override
    protected void initListeners() {
        this.setServerEventListener(new ServerEventListenerImpl());
    }

}
