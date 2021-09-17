package com.github.zh.engine.annotation.properties;

import java.lang.annotation.*;

/**
 * @author zhanghuan
 * @version 1.0
 * @date 2021/9/17 16:41
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Properties {

    Property[] value();
}
