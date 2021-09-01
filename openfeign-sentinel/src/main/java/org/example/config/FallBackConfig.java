package org.example.config;

import org.example.feign.FeignClientTest;
import org.springframework.stereotype.Component;

@Component
public class FallBackConfig implements FeignClientTest {

    @Override
    public String test() {
        return "服务被熔断了";
    }
}
