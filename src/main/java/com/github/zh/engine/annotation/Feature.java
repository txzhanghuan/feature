package com.github.zh.engine.annotation;

import java.lang.annotation.*;

/**
 * @author zhanghuan
 * @created 2020/01/27
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Feature {
    /**
     * 生成的类名
     *
     * @return
     */
    String name() default "";

    /**
     * 结果是否输出
     *
     * @return
     */
    boolean output() default true;
}
