package com.github.zh.engine;

import com.github.zh.engine.co.AbstractFeatureBean;
import com.github.zh.engine.co.FeatureContext;
import com.github.zh.engine.processor.FeatureProcessor;
import com.github.zh.engine.properties.FeatureProperties;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

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
@Slf4j
public class FeatureEngine implements InitializingBean {

    @Autowired
    private FeatureProperties featureProperties;

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    private int index = 0;

    private ThreadPoolExecutor calcPool;

    @Autowired
    FeatureProcessor featureProcessor;

    /**
     * 计算变量（仅本地FeatureBean）
     *
     * @param originDataMap 原始数据
     * @param calcFeatures  待计算变量集合
     * @return
     */
    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures) {
        return this.calc(originDataMap, calcFeatures, featureProperties.getCalcTimeout(), false);
    }

    /**
     * 计算变量（仅本地FeatureBean）
     *
     * @param originDataMap 原始数据
     * @param calcFeatures  待计算变量集合
     * @param timeout       超时时间
     * @return
     */
    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures, long timeout) {
        return this.calc(originDataMap, calcFeatures, timeout, false);
    }

    /**
     * 计算变量（仅本地FeatureBean）
     *
     * @param originDataMap 原始数据
     * @param calcFeatures  待计算变量集合
     * @param debug         是否debug模式
     * @return
     */
    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures, boolean debug) {
        return this.calc(originDataMap, calcFeatures, featureProperties.getCalcTimeout(), debug);
    }

    /**
     * 计算变量（仅本地FeatureBean）
     *
     * @param originDataMap 原始数据
     * @param calcFeatures  待计算变量集合
     * @param timeout       超时时间
     * @param debug         是否debug模式
     * @return
     */
    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures, long timeout, boolean debug) {
        log.debug("Start calculate!");
        FeatureContext featureContext = new FeatureContext();
        featureContext.init(calcPool, originDataMap, calcFeatures, featureProcessor.getFeatureBeanMap());
        try {
            featureContext.executeAll(timeout, MDC.getCopyOfContextMap());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return featureContext.getCalcResult(debug);
    }

    /**
     * 计算变量（本地FeatureBean和外部带入的Bean）
     *
     * @param originDataMap    原始数据
     * @param calcFeatures     待计算变量集合
     * @param outerFeatureBean 外部带入的计算Bean
     * @return
     */
    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean) {
        return this.calcWithOuterFeatureBean(originDataMap, calcFeatures, outerFeatureBean, featureProperties.getCalcTimeout(), false);
    }

    /**
     * 计算变量（本地FeatureBean和外部带入的Bean）
     *
     * @param originDataMap    原始数据
     * @param calcFeatures     待计算变量集合
     * @param outerFeatureBean 外部带入的计算Bean
     * @param timeout          超时时间
     * @return
     */
    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean, long timeout) {
        return this.calcWithOuterFeatureBean(originDataMap, calcFeatures, outerFeatureBean, timeout, false);
    }

    /**
     * 计算变量（本地FeatureBean和外部带入的Bean）
     *
     * @param originDataMap    原始数据
     * @param calcFeatures     待计算变量集合
     * @param outerFeatureBean 外部带入的计算Bean
     * @param debug            是否debug模式
     * @return
     */
    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean, boolean debug) {
        return this.calcWithOuterFeatureBean(originDataMap, calcFeatures, outerFeatureBean, featureProperties.getCalcTimeout(), debug);
    }

    /**
     * 计算变量（本地FeatureBean和外部带入的Bean）
     *
     * @param originDataMap    原始数据
     * @param calcFeatures     待计算变量集合
     * @param outerFeatureBean 外部带入的计算Bean
     * @param timeout          超时时间
     * @param debug            是否debug模式
     * @return
     */
    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean,
                                                        long timeout, boolean debug) {
        log.debug("Start calculate!");
        FeatureContext featureContext = new FeatureContext();
        featureContext.initWithOuterFeatureBean(calcPool, originDataMap, calcFeatures, featureProcessor.getFeatureBeanMap(), outerFeatureBean);
        try {
            featureContext.executeAll(timeout, MDC.getCopyOfContextMap());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return featureContext.getCalcResult(debug);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        calcPool = new ThreadPoolExecutor(featureProperties.getFeatureThreadPoolSize(), featureProperties.getFeatureThreadPoolMaxSize(), 0,
                TimeUnit.SECONDS, queue, r -> new Thread(r, "feature-pool-" + index++)
        );
    }
}
