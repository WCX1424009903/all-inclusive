package org.example.dubbo.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.example.dubbo.domain.Producer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@DubboService(loadbalance = "roundrobin")
@Service
public class ProductServiceImpl implements ProductService {

    @Override
    public Producer addProducer() {
        Producer producer = new Producer();
        producer.setUserName("用户名");
        producer.setUserId(10260L);
        producer.setCreateTime(LocalDateTime.now());
        return producer;

    }
}
