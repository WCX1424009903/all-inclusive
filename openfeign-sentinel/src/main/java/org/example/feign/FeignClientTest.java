package org.example.feign;

import org.example.config.FallBackConfig;
import org.example.feignclient.FeignClientInterface;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = FeignClientInterface.OPENFEIGN_SENTINEL,fallback = FallBackConfig.class)
public interface FeignClientTest {

    @RequestMapping(method = RequestMethod.GET,value = "/first")
    String test();

}
