package org.example.core.exception;

import org.example.core.enums.StatusCodeEnum;
import org.example.core.result.R;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理
 * @author wcx
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     *验证类异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R verifyExcption(MethodArgumentNotValidException exception) {
        // 获取异常信息
        BindingResult exceptions = exception.getBindingResult();
        // 判断异常中是否有错误信息，如果存在就使用异常中的消息，否则使用默认消息
        if (exceptions.hasErrors()) {
            List<ObjectError> errors = exceptions.getAllErrors();
            if (!errors.isEmpty()) {
                // 这里列出了全部错误参数，按正常逻辑，只需要第一条错误即可
                FieldError fieldError = (FieldError) errors.get(0);
                return R.builder()
                        .code(StatusCodeEnum.VERIFY_FAILS.getCode())
                        .message(fieldError.getDefaultMessage()).build();
            }
        }
        return R.builder()
                .code(StatusCodeEnum.VERIFY_FAILS.getCode())
                .message(StatusCodeEnum.VERIFY_FAILS.getMessage()).build();
    }
    /**
     *自定义异常处理
     */
    @ExceptionHandler(CustomizeException.class)
    public R customizeExcption(CustomizeException customizeException) {
        customizeException.printStackTrace();
        return R.builder()
                .code(StatusCodeEnum.INTERNAL_SERVER_ERROR.getCode())
                .message(customizeException.getMessage()).build();
    }
    /**
     * 空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public R nullpointExcption(NullPointerException nullPointerException) {
        nullPointerException.printStackTrace();
        return R.builder()
                .code(StatusCodeEnum.NULL_POINT_FAILS.getCode())
                .message(StatusCodeEnum.NULL_POINT_FAILS.getMessage()).build();
    }
    /**
     * 未知异常,服务器内部错误
     */
    @ExceptionHandler(Exception.class)
    public R exception(Exception exception) {
        exception.printStackTrace();
        return R.builder()
                .code(StatusCodeEnum.INTERNAL_SERVER_ERROR.getCode())
                .message(StatusCodeEnum.INTERNAL_SERVER_ERROR.getMessage()).build();
    }
}
