package com.github.zh.engine.exception;

/**
 * @author 阿桓
 * Date: 2020/4/3
 * Time: 10:34 上午
 * Description:
 */
public class CalculateException extends RuntimeException {

    public CalculateException(String message) {
        super(message);
    }

    public CalculateException(Throwable e) {
        super(e);
    }

}