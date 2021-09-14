package org.example.controller;

import org.example.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    private TokenStore tokenStore;

    @RequestMapping("/user/getCurrentUser")
    public R getCurrentUser(@RequestParam("access_token") String accessToken) {
        OAuth2Authentication oAuth2Authentication = tokenStore.readAuthentication(accessToken);
        User user = (User) oAuth2Authentication.getUserAuthentication().getPrincipal();
        return R.ok(user);
    }

}
