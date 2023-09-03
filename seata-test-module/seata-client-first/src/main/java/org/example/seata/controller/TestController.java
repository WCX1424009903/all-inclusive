package org.example.seata.controller;

import jakarta.annotation.Resource;
import org.example.core.result.R;
import org.example.seata.domain.First;
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
@RequestMapping("/seata-first")
public class TestController {
    @Resource
    private TestService testService;

    /**
    * 服务失败
    */
    @PostMapping("/add")
    public R add(@RequestBody First first) {
        testService.add(first);
        return R.ok();
    }

}
