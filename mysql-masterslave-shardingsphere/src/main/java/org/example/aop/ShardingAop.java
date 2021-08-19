package org.example.aop;

import java.lang.annotation.*;

/**
 * sharing-sphere分片自定义注解
 * 原理是通过hintManager进行强制路由，详情：https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/usage/sharding/hint/
 * @author wcx
 * @date 2021/8/18 20:55
 */
@Documented
@Inherited
@Target({ElementType.METHOD,ElementType.TYPE,ElementType.ANNOTATION_TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ShardingAop {
    /**
     * 分片值
     */
    String value() default "";
    /**
     * 分片表
     */
    String[] tables() default {};
}
