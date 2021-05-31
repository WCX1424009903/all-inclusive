package org.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

@RefreshScope
@RestController
@RequestMapping("/configInfo")
public class ConfigInfoController {

    @Value("${server.port}")
    public String port;
    @GetMapping("/test")
    public String test(){
        return port;
    }

}
