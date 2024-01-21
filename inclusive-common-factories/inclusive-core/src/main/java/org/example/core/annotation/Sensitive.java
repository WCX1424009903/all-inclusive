package org.example.core.annotation;

import org.example.core.enums.SensitiveTypeEnum;

/**
 * 脱敏注解
 *
 * @author wcx
 * @date 2024/1/21
 */
public @interface Sensitive {
    /**
     * 脱敏数据类型
     */
    SensitiveTypeEnum type() default SensitiveTypeEnum.CUSTOMER;

    /**
     * 前置不需要打码的长度
     */
    int prefixNoMaskLen() default 0;

    /**
     * 后置不需要打码的长度
     */
    int suffixNoMaskLen() default 0;

    /**
     * 用什么打码
     */
    String symbol() default "*";
}
