package org.example.enums;

import lombok.Getter;

/**
 * 返回状态码及文本信息
 * @author wcx
 */
@Getter
public enum StatusCodeEnum {
    /**
     *成功状态码
     */
    SUCCESS(200,"成功"),
    /**
     *失败状态码
     */
    FAILS(400,"失败"),
    /**
     *验证类异常状态码
     */
    VERIFY_FAILS(403,"验证错误"),
    /**
     * 空指针异常
     */
    NULL_POINT_FAILS(402,"空指针异常"),
    /**
     *服务器内部报错状态码
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误!");



    private int code;
    private String message;
    StatusCodeEnum(int code,String message){
        this.code = code;
        this.message = message;
    }
}
