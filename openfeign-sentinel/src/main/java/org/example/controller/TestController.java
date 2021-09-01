package org.example.controller;

import org.example.feign.FeignClientTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private FeignClientTest feignClientTest;

    @GetMapping("/first")
    public String test() {
        return "feign调用";
    }

    @GetMapping("/temp")
    public String temp() {
        return feignClientTest.test();
    }

}
