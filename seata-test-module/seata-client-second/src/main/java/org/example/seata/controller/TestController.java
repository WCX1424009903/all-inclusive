package org.example.seata.controller;

import jakarta.annotation.Resource;
import org.example.core.result.R;
import org.example.seata.domain.Second;
import org.example.seata.service.TestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
* seata服务调用
* @author wcx
* @date 2022/12/4
*/
@RestController
@RequestMapping("/seata-second")
public class TestController {
    @Resource
    private TestService testService;

    /**
    * 服务失败
    */
    @PostMapping("/add")
    public R<Long> add(@RequestBody Second second) {
        return R.ok(testService.add(second));
    }

}
