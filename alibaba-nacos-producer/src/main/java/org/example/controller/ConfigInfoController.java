package org.example.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class ConfigInfoController {

    @Value("${server.port}")
    public String port;

    @RequestMapping("/test")
    public String test(){
        return port;
    }

}
