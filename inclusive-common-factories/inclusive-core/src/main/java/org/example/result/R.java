package org.example.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.enums.StatusCodeEnum;

import java.io.Serializable;

/**
 * 返回结果
 * @author wcx
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private String message;
    private T data;
    private int code;

    /**
     * 失败方法
     */
    public static R fail(Object data) {
        R r = new R();
        r.setData(data);
        r.setCode(StatusCodeEnum.FAILS.getCode());
        r.setMessage(StatusCodeEnum.FAILS.getMessage());
        return r;
    }
    /**
     * 成功方法
     */
    public static R ok(Object data) {
      R r = new R();
      r.setData(data);
      r.setCode(StatusCodeEnum.SUCCESS.getCode());
      r.setMessage(StatusCodeEnum.SUCCESS.getMessage());
      return r;
    }

    public static R ok() {
        R r = new R();
        r.setCode(StatusCodeEnum.SUCCESS.getCode());
        r.setMessage(StatusCodeEnum.SUCCESS.getMessage());
        return r;
    }

}
