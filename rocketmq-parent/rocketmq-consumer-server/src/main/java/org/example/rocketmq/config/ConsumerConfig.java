package org.example.rocketmq.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

/**
 * rocketmq消费者
 *
 * @author wcx
 * @date 2024/04/24
 */
@Configuration
public class ConsumerConfig {

    @Bean
    public Consumer<Message<String>> consumerHandler() {
        return message -> {
            System.out.println(Thread.currentThread().getName() + " Consumer Receive New Messages: " + message.getPayload());
        };
    }

}
