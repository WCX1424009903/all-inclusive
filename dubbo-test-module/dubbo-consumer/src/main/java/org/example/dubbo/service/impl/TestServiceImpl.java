package org.example.dubbo.service.impl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.example.dubbo.domain.Producer;
import org.example.dubbo.service.ProductService;
import org.example.dubbo.service.TestService;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {

    @DubboReference(loadbalance = "roundrobin")
    private ProductService productService;

    @Override
    public Producer get() {
        return productService.addProducer();
    }
}
