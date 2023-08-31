package org.example.controller.rabbitmq;

import cn.hutool.json.JSONUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.rabbitmq.constant.RabbitmqConstant;
import org.example.domain.RabbitmqObject;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
*rabbitmq消费者
*@author wcx
*@date 2022/11/19 17:11
*/
@Component
public class RabbitmqReceiver {

    public static final Log log = LogFactory.getLog(RabbitmqReceiver.class);

    /**
    *queue属性可通过占位符的形式从配置中心获取${rabbitmq.queue}
    */
    @RabbitListener(queues = {RabbitmqConstant.QUEUE_FRIST})
    @RabbitHandler
    public void handlerFirstOne(RabbitmqObject rabbitmqObject) {
        log.info(RabbitmqConstant.QUEUE_FRIST+JSONUtil.toJsonStr(rabbitmqObject));

    }

    @RabbitListener(queues = {RabbitmqConstant.QUEUE_SECOND})
    @RabbitHandler
    public void handlerSecond(String message) {
        log.info(RabbitmqConstant.QUEUE_SECOND+message);
    }

}
