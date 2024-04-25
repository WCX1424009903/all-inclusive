package org.example.rocketmq.controller;

import jakarta.annotation.Resource;
import org.example.core.result.R;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * rocketmq生产者
 *
 * @author wcx
 * @date 2024/04/24
 */
@RestController
@RequestMapping("/producer")
public class ProducerController {

    @Resource
    private StreamBridge streamBridge;

    /**
     * 发送普通消息
     *
     * @param message 消息
     */
    @GetMapping("/send")
    public R sendMessage(String message) {
        Message<String> stringMessage = new GenericMessage<>(message);
        boolean send = streamBridge.send("producer-output-0", stringMessage);
        return R.ok(send);
    }


}
