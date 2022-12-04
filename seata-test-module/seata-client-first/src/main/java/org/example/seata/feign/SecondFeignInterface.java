package org.example.seata.feign;

import org.example.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
*远程调用second服务
* @author wcx
* @date 2022/12/4
*/
@FeignClient(value = "seata-client-second",contextId = "SecondFeignInterface",configuration = {XidRequestInterceptor.class})
public interface SecondFeignInterface {

    @PostMapping("/seata-second/add")
    R<Long> add(@RequestBody Second second);

}
