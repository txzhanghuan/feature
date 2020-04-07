package com.github.zh.engine.annotation;

import java.lang.annotation.*;

/**
 * @author zhanghuan
 * @created 2020/01/27
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Feature {
    /**
     * 生成的类名，首字母变成大写
     *
     * @return
     */
    String name();

    /**
     * 变量的描述
     *
     * @return
     */
    String description() default "";

    /**
     * 结果是否输出
     *
     * @return
     */
    boolean output() default true;
}
