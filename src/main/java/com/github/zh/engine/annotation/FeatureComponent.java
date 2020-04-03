package com.github.zh.engine.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author zhanghuan
 * @created 2020/01/27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Inherited
public @interface FeatureComponent {

    String preName() default "";
}
