package org.example.core.exception;

/**
 * 自定义异常
 * @author wcx
 */
public class CustomizeException extends RuntimeException{

    public CustomizeException(){}

    public CustomizeException(String message){
        super(message);
    }

}
