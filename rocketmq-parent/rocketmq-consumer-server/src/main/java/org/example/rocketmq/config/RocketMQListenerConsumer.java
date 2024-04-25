package org.example.rocketmq.config;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@Import(RocketMQAutoConfiguration.class)
@RocketMQMessageListener(topic = "rocketmq-normal-message-topic", consumerGroup = "consumerHandler-group")
public class RocketMQListenerConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        System.out.println("接收到的消息为: " + message);
    }

}
