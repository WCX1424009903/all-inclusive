package org.example.rocketmq.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * rocketmq消费者
 *
 * @author wcx
 * @date 2024/04/24
 */
@Configuration
@Slf4j
public class ConsumerConfig {

    /**
     * 消费者处理程序，不能返回message<String>，否则消息不会消费
     *
     * @return {@link Consumer}<{@link String}>
     */
    @Bean
    public Consumer<String> consumerHandler() {
        return message -> {
            log.info(Thread.currentThread().getName() + " Consumer Receive New Messages: " + message);
        };
    }

}
