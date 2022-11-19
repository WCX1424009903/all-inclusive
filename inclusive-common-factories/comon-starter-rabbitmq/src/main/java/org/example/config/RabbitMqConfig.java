package org.example.config;

import org.example.constant.RabbitmqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
*rabbitmq队列与交换机配置
*@author wcx
*@date 2022/11/19 17:07
*/
@Configuration
public class RabbitMqConfig {

    /**
    *可以从配置中心获取对应的交换机和队列名
    */
    @Bean
    public Queue queue_one() {
        return new Queue(RabbitmqConstant.QUEUE_FRIST);
    }

    @Bean
    public Queue queue_two(){
        return new Queue(RabbitmqConstant.QUEUE_SECOND);
    }

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(RabbitmqConstant.TOPIC_EXCHANGE);
    }

    @Bean
    public Binding bindingExchangeOne(Queue queue_one, TopicExchange exchange){
        return BindingBuilder.bind(queue_one).to(exchange).with(RabbitmqConstant.QUEUE_FRIST);
    }

    @Bean
    public Binding bindingExchangeTwo(Queue queue_two, TopicExchange exchange){
        //# 表示零个或多个词
        //* 表示一个词
        return BindingBuilder.bind(queue_two).to(exchange).with("topic.#");
    }

}
