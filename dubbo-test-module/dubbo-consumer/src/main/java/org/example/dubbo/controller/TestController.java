package org.example.dubbo.controller;

import org.example.dubbo.domain.Producer;
import org.example.dubbo.service.TestService;
import org.example.core.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private TestService testService;

    @GetMapping
    public R<Producer> get() {
        Producer producer = testService.get();
        return R.ok(producer);
    }

}
