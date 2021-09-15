package org.example.utils;

import cn.hutool.core.util.StrUtil;
import org.example.exception.CustomizeException;

/**
 * 断言工具类
 * @author wcx
 * @date 2021/9/12 17:22
 */
public class AssertUtil {
    /**
     * 自定义异常错误
     */
    public static void cumstomException(String message) {
        if (StrUtil.isNotBlank(message)) {
            throw new CustomizeException(message);
        }
    }

}
