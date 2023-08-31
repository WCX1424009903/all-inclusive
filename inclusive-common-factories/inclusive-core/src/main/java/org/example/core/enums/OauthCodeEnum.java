package org.example.core.enums;

import lombok.Getter;

/**
 * oauth授权状态码
 * @author wcx
 * @date 2021/9/12 17:25
 */
@Getter
public enum OauthCodeEnum {
    /**
     * 密码错误
     */
    USERNAME_PASSWORD_ERROR(100,"密码不正确"),
    /**
     * 无权访问,请求头为空
     */
    UNAUTHORIZED_HEADER_IS_EMPTY(102,"无权访问"),
    /**
     * 未找到该用户
     */
    USERNAME_NOT_FIND(101,"未找到该用户账号"),
    /**
     * 登录已过期
     */
    LOGIN_EXPIRED(103,"暂未登录或登录已过期");

    private int code;
    private String message;
    OauthCodeEnum(int code,String message){
        this.code = code;
        this.message = message;
    }
}
