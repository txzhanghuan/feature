package com.github.zh.engine.exception;

import org.springframework.beans.BeansException;

/**
 * @author 阿桓
 * Date: 2020/4/14
 * Time: 2:49 下午
 * Description:
 */
public class FeatureCreationException extends BeansException {

    public FeatureCreationException(String msg) {
        super(msg);
    }

    public FeatureCreationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
