package org.example.kafka.controller;

import jakarta.annotation.Resource;
import org.example.core.result.R;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
*kafka生产者
* @author wcx
* @date 2022/12/13
*/
@RestController
@RequestMapping("/kafka")
public class ProducerController {

    @Resource
    private KafkaTemplate<String,Object> kafkaTemplate;

    @GetMapping
    public R test(String test) {
        kafkaTemplate.send("test_topic",test);
        return R.ok();
    }

}
