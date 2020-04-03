package engine.config;

import engine.FeatureEngine;
import engine.processor.FeatureProcessor;
import engine.properties.FeatureProperties;
import org.springframework.beans.factory.annotation.Autowired;
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
@ConditionalOnProperty(name = "enabled.autoConfituration", matchIfMissing = true)
@ConditionalOnClass({FeatureEngine.class})
@EnableConfigurationProperties(FeatureProperties.class)
public class FeatureAutoConfiguration {

    @Autowired
    private FeatureProperties featureProperties;

    @Bean
    @ConditionalOnMissingBean
    public FeatureEngine getFeatureEngine(){
        FeatureEngine featureEngine = new FeatureEngine();
        featureEngine.setFeatureProperties(featureProperties);
        return featureEngine;
    }

}
