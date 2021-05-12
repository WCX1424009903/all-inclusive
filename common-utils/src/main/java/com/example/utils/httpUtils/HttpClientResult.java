package com.example.utils.httpUtils;

import lombok.Data;

import java.io.Serializable;

/**
 *
 */
@Data
public class HttpClientResult implements Serializable {

    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应数据
     */
    private String content;
    
    public HttpClientResult(String content) {
    	this.content = content;
    }

    public HttpClientResult() {
    }
    
    public HttpClientResult(Integer code) {
    	this.code = code;
    }
    
    public HttpClientResult(Integer code, String content) {
    	this.code = code;
    	this.content = content;
    }

}