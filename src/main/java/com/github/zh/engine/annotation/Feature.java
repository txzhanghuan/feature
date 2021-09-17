package com.github.zh.engine.annotation;

import java.lang.annotation.*;

/**
 * @author zhanghuan
 * @date 2020/01/27
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Feature {
    /**
     * 生成的类名
     *
     * @return 特征名称
     */
    String name() default "";

    /**
     * 结果是否输出
     *
     * @return true/false
     */
    boolean output() default true;
}
