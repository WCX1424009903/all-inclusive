package org.example.persistence.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {
    /**
     * 是否启用
     */
    boolean enabled() default true;

    /**
     * 表别名
     */
    String tableAlias() default "";

    /**
     * 表字段
     */
    String tableField() default "F_UserId";

}
