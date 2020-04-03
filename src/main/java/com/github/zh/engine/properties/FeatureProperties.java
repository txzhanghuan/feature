package com.github.zh.engine.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 阿桓
 * Date: 2020/4/2
 * Time: 2:54 下午
 * Description:
 */
@Data
@ConfigurationProperties(prefix = "com.github.zh.feature")
@Configuration
public class FeatureProperties {

    /**
     * 机器核心数
     */
    public static final Integer DEFAULT_SYSTEM_CORE_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * 默认计算超时时间
     */
    public static final Integer DEFAULT_CALC_TIMEOUT = 10000;

    /**
     * 默认线程数为机器核心数*2
     */
    private Integer featureThreadPoolSize = DEFAULT_SYSTEM_CORE_SIZE * 2;

    private Integer featureThreadPoolMaxSize = DEFAULT_SYSTEM_CORE_SIZE * 2;

    private Integer calcTimeout = DEFAULT_CALC_TIMEOUT;
}
