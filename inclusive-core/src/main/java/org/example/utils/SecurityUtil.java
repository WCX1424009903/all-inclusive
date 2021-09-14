package org.example.utils;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 *从请求头中获取用户信息工具类
 * @author wcx
 * @date 2021/9/13 21:58
 */
@Slf4j
public class SecurityUtil {

    /**
     *获取登录用户信息
     *返回用户实体类，通过common-model工程引入
     */
    public static void getCurrentUser() {
        // 从Header中获取用户信息
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String userStr = request.getHeader("user");
        log.info("{当前登录用户信息:}"+userStr);
    }



}
