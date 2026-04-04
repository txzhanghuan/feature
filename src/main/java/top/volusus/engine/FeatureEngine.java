/*
 * Copyright (C) 2026 zhanghuan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package top.volusus.engine;

import top.volusus.engine.co.AbstractFeatureBean;
import top.volusus.engine.co.FeatureContext;
import top.volusus.engine.processor.NativeFeatureProcessor;
import top.volusus.engine.properties.FeatureProperties;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The core engine for feature computation based on a DAG (Directed Acyclic Graph) dependency model.
 * <p>
 * This class provides the main entry point for executing feature calculations. It manages a thread pool
 * for parallel computation and supports both native (locally defined) and outer (externally provided)
 * feature beans.
 * </p>
 * <p>
 * <b>Thread Safety:</b> This class is thread-safe. Multiple threads can invoke calculation methods
 * concurrently, with each invocation using an isolated {@link FeatureContext}.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * @Autowired
 * private FeatureEngine featureEngine;
 *
 * Map<String, Object> originData = new HashMap<>();
 * originData.put("input1", value1);
 * Set<String> features = Set.of("feature1", "feature2");
 * Map<String, Object> result = featureEngine.calc(originData, features);
 * }
 * </pre>
 * </p>
 *
 * @author 阿桓
 * Date: 2020/3/25
 * Time: 9:08 下午
 * @see FeatureContext
 * @see NativeFeatureProcessor
 * @see AbstractFeatureBean
 */
@Slf4j
@Component
public class FeatureEngine implements InitializingBean, DisposableBean {

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(1024);
    @Autowired
    private FeatureProperties featureProperties;
    private int index = 0;

    @Setter
    private ThreadPoolExecutor calcPool;

    @Autowired
    private NativeFeatureProcessor nativeFeatureProcessor;

    /**
     * Calculates features using only native (locally defined) FeatureBeans.
     * <p>
     * Uses the default timeout configured in {@link FeatureProperties#getCalcTimeout()}.
     * </p>
     *
     * @param originDataMap the original input data map where keys are parameter names and values are input values
     * @param calcFeatures  the set of feature names to be calculated
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     * @see #calc(Map, Set, long)
     * @see #calcWithOuterFeatureBean(Map, Set, Map)
     */
    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures) {
        return doCalc(originDataMap, calcFeatures, featureProperties.getCalcTimeout(), calcPool, false, null);
    }

    /**
     * Calculates features using only native (locally defined) FeatureBeans with a custom timeout.
     *
     * @param originDataMap the original input data map where keys are parameter names and values are input values
     * @param calcFeatures  the set of feature names to be calculated
     * @param timeout       the maximum time in milliseconds to wait for calculation to complete
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     * @see #calc(Map, Set)
     */
    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures, long timeout) {
        return doCalc(originDataMap, calcFeatures, timeout, calcPool, false, null);
    }

    /**
     * Calculates features using only native (locally defined) FeatureBeans with debug mode option.
     * <p>
     * When debug mode is enabled, the result includes all intermediate feature values,
     * not just the output features.
     * </p>
     *
     * @param originDataMap the original input data map where keys are parameter names and values are input values
     * @param calcFeatures  the set of feature names to be calculated
     * @param debug         if true, returns all computed values including intermediate results;
     *                      if false, returns only features marked as output
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     * @see #calc(Map, Set, long, ThreadPoolExecutor, boolean)
     */
    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures, boolean debug) {
        return doCalc(originDataMap, calcFeatures, featureProperties.getCalcTimeout(), calcPool, debug, null);
    }

    /**
     * Calculates features using only native (locally defined) FeatureBeans with full customization.
     * <p>
     * This is the most flexible variant allowing custom timeout, thread pool, and debug mode.
     * </p>
     *
     * @param originDataMap  the original input data map where keys are parameter names and values are input values
     * @param calcFeatures   the set of feature names to be calculated
     * @param timeout        the maximum time in milliseconds to wait for calculation to complete
     * @param calculcatePool the thread pool executor to use for parallel computation
     * @param debug          if true, returns all computed values including intermediate results;
     *                       if false, returns only features marked as output
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     */
    public Map<String, Object> calc(Map<String, Object> originDataMap, Set<String> calcFeatures, long timeout, ThreadPoolExecutor calculcatePool, boolean debug) {
        return doCalc(originDataMap, calcFeatures, timeout, calculcatePool, debug, null);
    }

    /**
     * Calculates features using both native (locally defined) and outer (externally provided) FeatureBeans.
     * <p>
     * Uses the default timeout configured in {@link FeatureProperties#getCalcTimeout()}.
     * Outer feature beans can be used to inject dynamic computation logic at runtime.
     * </p>
     *
     * @param originDataMap    the original input data map where keys are parameter names and values are input values
     * @param calcFeatures     the set of feature names to be calculated
     * @param outerFeatureBean a map of externally provided feature beans to include in computation
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     * @see #calc(Map, Set)
     */
    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean) {
        return doCalc(originDataMap, calcFeatures, featureProperties.getCalcTimeout(), calcPool, false, outerFeatureBean);
    }

    /**
     * Calculates features using both native and outer FeatureBeans with a custom timeout.
     *
     * @param originDataMap    the original input data map where keys are parameter names and values are input values
     * @param calcFeatures     the set of feature names to be calculated
     * @param outerFeatureBean a map of externally provided feature beans to include in computation
     * @param timeout          the maximum time in milliseconds to wait for calculation to complete
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     */
    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean, long timeout) {
        return doCalc(originDataMap, calcFeatures, timeout, calcPool, false, outerFeatureBean);
    }

    /**
     * Calculates features using both native and outer FeatureBeans with debug mode option.
     * <p>
     * When debug mode is enabled, the result includes all intermediate feature values.
     * </p>
     *
     * @param originDataMap    the original input data map where keys are parameter names and values are input values
     * @param calcFeatures     the set of feature names to be calculated
     * @param outerFeatureBean a map of externally provided feature beans to include in computation
     * @param debug            if true, returns all computed values including intermediate results;
     *                         if false, returns only features marked as output
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     */
    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean, boolean debug) {
        return doCalc(originDataMap, calcFeatures, featureProperties.getCalcTimeout(), calcPool, debug, outerFeatureBean);
    }

    /**
     * Calculates features using both native and outer FeatureBeans with full customization.
     * <p>
     * This is the most flexible variant for outer feature bean computation, allowing custom
     * timeout, thread pool, and debug mode.
     * </p>
     *
     * @param originDataMap    the original input data map where keys are parameter names and values are input values
     * @param calcFeatures     the set of feature names to be calculated
     * @param outerFeatureBean a map of externally provided feature beans to include in computation
     * @param timeout          the maximum time in milliseconds to wait for calculation to complete
     * @param calculatePool    the thread pool executor to use for parallel computation
     * @param debug            if true, returns all computed values including intermediate results;
     *                         if false, returns only features marked as output
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     */
    public Map<String, Object> calcWithOuterFeatureBean(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                                        Map<String, ? extends AbstractFeatureBean> outerFeatureBean,
                                                        long timeout, ThreadPoolExecutor calculatePool, boolean debug) {
        return doCalc(originDataMap, calcFeatures, timeout, calculatePool, debug, outerFeatureBean);
    }

    /**
     * Core calculation method that all public calc methods delegate to.
     * <p>
     * This method creates a new {@link FeatureContext}, initializes it with the provided data,
     * executes all required features in parallel using the DAG dependency model, and returns
     * the results.
     * </p>
     *
     * @param originDataMap       the original input data map where keys are parameter names and values are input values
     * @param calcFeatures        the set of feature names to be calculated
     * @param timeout             the maximum time in milliseconds to wait for calculation to complete
     * @param executePool         the thread pool executor to use for parallel computation
     * @param debug               if true, returns all computed values including intermediate results;
     *                            if false, returns only features marked as output
     * @param outerFeatureBeanMap a map of externally provided feature beans, or null for native-only computation
     * @return a map containing feature names and their computed values
     * @throws IllegalArgumentException if calcFeatures is null or empty
     * @throws CalculateException       if calculation times out or fails
     */
    private Map<String, Object> doCalc(Map<String, Object> originDataMap, Set<String> calcFeatures,
                                       long timeout, ThreadPoolExecutor executePool, boolean debug,
                                       Map<String, ? extends AbstractFeatureBean> outerFeatureBeanMap) {
        if (calcFeatures == null || calcFeatures.isEmpty()) {
            throw new IllegalArgumentException("calcFeatures must not be null or empty");
        }
        log.debug("Start calculate!");
        FeatureContext featureContext = new FeatureContext();
        Map<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();
        if (outerFeatureBeanMap == null) {
            featureContext.init(executePool, originDataMap, calcFeatures, featureBeanMap);
        } else {
            featureContext.initWithOuterFeatureBean(executePool, originDataMap, calcFeatures, featureBeanMap, outerFeatureBeanMap);
        }
        try {
            featureContext.executeAll(timeout, MDC.getCopyOfContextMap());
        } catch (InterruptedException e) {
            log.error("Feature calculation was interrupted", e);
            Thread.currentThread().interrupt();
        }
        return featureContext.getCalcResult(debug);
    }

    /**
     * Initializes the thread pool after bean properties are set.
     * <p>
     * If no custom thread pool is provided via {@link #setCalcPool(ThreadPoolExecutor)},
     * this method creates a default thread pool based on the configuration in {@link FeatureProperties}.
     * </p>
     *
     * @throws Exception if initialization fails
     * @see InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (calcPool == null) {
            calcPool = new ThreadPoolExecutor(featureProperties.getFeatureThreadPoolSize(), featureProperties.getFeatureThreadPoolMaxSize(), 0,
                    TimeUnit.SECONDS, queue, r -> new Thread(r, featureProperties.getThreadPoolNamePrefix() + index++)
            );
        }
    }

    /**
     * Gracefully shuts down the thread pool when the bean is destroyed.
     * <p>
     * Attempts to wait up to 60 seconds for existing tasks to complete before forcing shutdown.
     * </p>
     *
     * @see DisposableBean#destroy()
     */
    @Override
    public void destroy() {
        if (calcPool != null) {
            calcPool.shutdown();
            try {
                if (!calcPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    calcPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                calcPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
