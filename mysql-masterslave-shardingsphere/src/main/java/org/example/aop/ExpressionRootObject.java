package org.example.aop;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表达式根对象
 * @author wcx
 * @date 2021/8/19 20:37
 */
@Data
@AllArgsConstructor
public class ExpressionRootObject {

    private Object object;

    private Object[] args;


}
