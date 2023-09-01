package org.example.security.controller;

import org.example.core.result.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {


    @RequestMapping("/user/getCurrentUser")
    public R getCurrentUser(@RequestParam("access_token") String accessToken) {

        return R.ok();
    }

}
