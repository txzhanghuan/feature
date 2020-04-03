package com.github.zh.engine;

import com.github.zh.engine.co.FeatureContext;
import com.github.zh.engine.processor.FeatureProcessor;
import com.github.zh.engine.co.AbstractFeatureBean;
import com.github.zh.engine.properties.FeatureProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 阿桓
 * Date: 2020/3/25
 * Time: 9:08 下午
 * Description:
 */
@Component
@Slf4j
public class FeatureEngine implements InitializingBean {

    @Getter
    @Setter
    private FeatureProperties featureProperties;

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private int index = 0;

    private ThreadPoolExecutor calcPool;

    @Autowired
    FeatureProcessor featureProcessor;

    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures){
        return this.calc(originDataMap, calcFeatures, featureProperties.getCalcTimeout());
    }

    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures, long timeout){
        log.info("Start calculate!");
        FeatureContext featureContext = new FeatureContext();
        featureContext.init(calcPool, originDataMap, calcFeatures, featureProcessor.getFeatureBeanMap());
        try {
            featureContext.executeAll(timeout, MDC.getCopyOfContextMap());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return featureContext.getCalcResult();
    }

    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean){
        return this.calcWithOuterFeatureBean(originDataMap, calcFeatures, outerFeatureBean, featureProperties.getCalcTimeout());
    }

    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean,
                                                        long timeout){
        log.info("Start calculate!");
        FeatureContext featureContext = new FeatureContext();
        featureContext.initWithOuterFeatureBean(calcPool, originDataMap, calcFeatures, featureProcessor.getFeatureBeanMap(), outerFeatureBean);
        try {
            featureContext.executeAll(timeout, MDC.getCopyOfContextMap());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return featureContext.getCalcResult();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        calcPool = new ThreadPoolExecutor(featureProperties.getFeatureThreadPoolSize(), featureProperties.getFeatureThreadPoolMaxSize(), 0,
                TimeUnit.SECONDS, queue, r -> new Thread(r, "feature-pool-" + index++)
        );
    }
}
