package org.example.controller.rabbitmq;

import org.example.rabbitmq.constant.RabbitmqConstant;
import org.example.domain.RabbitmqObject;
import org.example.core.result.R;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/rabbitmq")
public class RabbitmqTestController {

    @Resource
    private RabbitTemplate rabbitTemplate;


    @PostMapping("/add-first")
    public R add(@RequestBody RabbitmqObject rabbitmqObject) {
        rabbitTemplate.convertAndSend(RabbitmqConstant.QUEUE_FRIST,rabbitmqObject);
        return R.ok();
    }

    @PostMapping("/add-second")
    public R second(String message) {
        rabbitTemplate.convertAndSend(RabbitmqConstant.TOPIC_EXCHANGE,RabbitmqConstant.QUEUE_SECOND,message);
        return R.ok();
    }

}
