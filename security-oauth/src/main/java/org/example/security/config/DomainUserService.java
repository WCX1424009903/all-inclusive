package org.example.security.config;

import org.example.core.enums.OauthCodeEnum;
import org.example.core.utils.AssertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户资源管理类,用于与后端交互验证
 * @author wcx
 * @date 2021/9/12 17:06
 */
@Component
public class DomainUserService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 远程调用user服务，获取用户信息，此处采用写死方式
        if (!"admin".equals(username)) {
            AssertUtil.cumstomException(OauthCodeEnum.USERNAME_NOT_FIND.getMessage());
        }
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE"));
        User user = new User("admin",passwordEncoder.encode("123456"),authorityList);
        // 构建继承org.springframework.security.core.userdetails.User类即可实现扩展功能
        return user;
    }
}
