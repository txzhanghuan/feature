package com.github.zh.engine.annotation;

import java.lang.annotation.*;

/**
 * @author zhanghuan
 * @date 2020/01/27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface FeatureClass {

    String description() default "";
}
