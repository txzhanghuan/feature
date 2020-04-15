package com.github.zh.engine.config;

import com.github.zh.engine.FeatureEngine;
import com.github.zh.engine.processor.FeatureProcessor;
import com.github.zh.engine.properties.FeatureProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 阿桓
 * Date: 2020/4/3
 * Time: 5:18 下午
 * Description:
 */
@Configuration
@ConditionalOnProperty(name = "enabled.featureEngine", matchIfMissing = true)
@ConditionalOnClass({FeatureEngine.class, FeatureProcessor.class})
@EnableConfigurationProperties(FeatureProperties.class)
public class FeatureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(FeatureProcessor.class)
    public FeatureProcessor getFeatureProcessor() {
        return new FeatureProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(FeatureEngine.class)
    public FeatureEngine featureEngine() {
        return new FeatureEngine();
    }
}
