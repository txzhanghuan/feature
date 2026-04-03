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

package com.github.zh.engine.co;

import com.github.zh.engine.enums.FeatureEnums;
import com.github.zh.engine.enums.FeatureStates;
import com.github.zh.engine.exception.CalculateException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Execution context for a single feature computation request.
 * <p>
 * This class manages the lifecycle of a feature calculation, including:
 * <ul>
 *   <li>Maintaining a pool of {@link FeatureEntity} instances representing all features to be computed</li>
 *   <li>Managing the thread pool for parallel execution</li>
 *   <li>Tracking computation completion via {@link CountDownLatch}</li>
 *   <li>Handling fast-fail scenarios when any feature computation fails</li>
 * </ul>
 * </p>
 * <p>
 * <b>Thread Safety:</b> This class uses {@link ConcurrentHashMap} for thread-safe access to feature entities.
 * The {@code fastFail} flag is volatile to ensure visibility across threads.
 * </p>
 * <p>
 * <b>Lifecycle:</b> Each computation request should create a new FeatureContext instance.
 * The context should not be reused across multiple computation requests.
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/3/20
 * Time: 3:09 PM
 * @see FeatureEntity
 * @see FeatureEngine
 */
@Slf4j
public class FeatureContext {

    @Getter
    private final ConcurrentHashMap<String, FeatureEntity> featureEntitiesPool = new ConcurrentHashMap<>();

    @Getter
    private ThreadPoolExecutor pool;

    @Getter
    private CountDownLatch countDownLatch;

    @Getter
    @Setter
    private volatile boolean fastFail = false;

    /**
     * The number of features that need to be calculated.
     */
    private int needCalcFeaturesCount = 0;

    /**
     * Executes all feature calculations in parallel.
     * <p>
     * This method submits all feature entities to the thread pool for async execution
     * and waits for completion using a {@link CountDownLatch} with the specified timeout.
     * </p>
     *
     * @param timeout    the maximum time in milliseconds to wait for all calculations to complete
     * @param logContext the MDC log context to propagate to worker threads, may be null
     * @throws InterruptedException if the waiting thread is interrupted
     * @throws CalculateException   if no features are available to calculate or if calculation times out
     */
    public void executeAll(long timeout, Map<String, String> logContext) throws InterruptedException {
        if (needCalcFeaturesCount == 0) {
            throw new CalculateException("No features need to be calculated");
        }
        featureEntitiesPool.values().forEach(
                featureEntity -> CompletableFuture.runAsync(
                        () -> featureEntity.execute(logContext)
                        , pool)
        );
        if (!this.countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
            throw new CalculateException("Calculation timeout");
        }
    }

    /**
     * Initializes the computation context for native (locally defined) feature calculation.
     * <p>
     * Sets up the thread pool, injects origin data, initializes required feature entities,
     * resolves dependencies, and prepares the countdown latch.
     * </p>
     *
     * @param pool           the thread pool executor for parallel computation
     * @param originDataMap  the original input data map where keys are parameter names
     * @param calcFeatures   the set of feature names to be calculated
     * @param featureBeanMap the map of available native feature beans
     * @see #initWithOuterFeatureBean(ThreadPoolExecutor, Map, Set, Map, Map)
     */
    public void init(ThreadPoolExecutor pool, Map<String, Object> originDataMap, Set<String> calcFeatures, Map<String, AbstractFeatureBean> featureBeanMap) {
        doInit(pool, originDataMap, calcFeatures, featureBeanMap, null);
    }

    /**
     * Initializes the computation context with external (outer) feature beans.
     * <p>
     * This method extends the standard initialization by also incorporating
     * externally provided feature beans into the computation graph.
     * </p>
     *
     * @param pool              the thread pool executor for parallel computation
     * @param originDataMap     the original input data map where keys are parameter names
     * @param calcFeatures      the set of feature names to be calculated
     * @param featureBeanMap    the map of available native feature beans
     * @param outerFeatureBeanMap the map of externally provided feature beans
     * @see #init(ThreadPoolExecutor, Map, Set, Map)
     */
    public void initWithOuterFeatureBean(ThreadPoolExecutor pool, Map<String, Object> originDataMap, Set<String> calcFeatures,
                                         Map<String, AbstractFeatureBean> featureBeanMap,
                                         Map<String, ? extends AbstractFeatureBean> outerFeatureBeanMap) {
        doInit(pool, originDataMap, calcFeatures, featureBeanMap, outerFeatureBeanMap);
    }

    /**
     * Core initialization method that extracts common logic from public init methods.
     * <p>
     * Performs the following steps:
     * <ol>
     *   <li>Sets up the thread pool reference</li>
     *   <li>Delegates DAG construction to {@link FeatureDAGBuilder}</li>
     *   <li>Initializes the countdown latch</li>
     * </ol>
     * </p>
     *
     * @param pool                the thread pool executor for parallel computation
     * @param originDataMap       the original input data map where keys are parameter names
     * @param calcFeatures        the set of feature names to be calculated
     * @param featureBeanMap      the map of available native feature beans
     * @param outerFeatureBeanMap the map of externally provided feature beans, or null for native-only
     */
    private void doInit(ThreadPoolExecutor pool, Map<String, Object> originDataMap, Set<String> calcFeatures,
                        Map<String, AbstractFeatureBean> featureBeanMap,
                        Map<String, ? extends AbstractFeatureBean> outerFeatureBeanMap) {
        // Set up the thread pool
        this.pool = pool;

        // Build DAG using FeatureDAGBuilder
        FeatureDAGBuilder dagBuilder = new FeatureDAGBuilder(
                this,
                featureEntitiesPool,
                featureBeanMap,
                originDataMap,
                calcFeatures,
                outerFeatureBeanMap
        );
        this.needCalcFeaturesCount = dagBuilder.build();

        // Initialize the countdown latch for feature count
        this.countDownLatch = new CountDownLatch(needCalcFeaturesCount);
    }

    /**
     * Checks if any feature computation has failed and throws an exception.
     * <p>
     * If fast-fail mode is active, finds the root cause of the failure
     * and throws a {@link CalculateException} with details.
     * </p>
     *
     * @throws CalculateException if any feature has failed during computation
     */
    private void checkFail() {
        if (fastFail) {
            FeatureEntity featureEntity = featureEntitiesPool.values().stream().filter(
                    it -> it.getStatus().get().equals(FeatureStates.FAILED)
                            && it.getError() != null).findFirst().get();
            String errorFeature = getRootErrorFeature(featureEntity);
            Throwable rootCause = featureEntitiesPool.get(errorFeature).getError();
            log.error("Calculate failed! The root failed feature is {}", errorFeature, rootCause);
            throw new CalculateException("Calculate failed at feature: " + errorFeature, rootCause);
        }
    }

    /**
     * Retrieves the calculation results from all computed features.
     * <p>
     * Before returning results, this method checks if any feature failed.
     * If fast-fail was triggered, it throws a {@link CalculateException} with
     * details about the root cause.
     * </p>
     *
     * @param debug if true, returns all computed values including origin data and intermediate results;
     *              if false, returns only features marked as output
     * @return a map of feature names to their computed values
     * @throws CalculateException if any feature computation failed
     */
    public Map<String, Object> getCalcResult(boolean debug) {
        checkFail();
        Map<String, Object> resultMap = new HashMap<>(needCalcFeaturesCount);
        if (debug) {
            featureEntitiesPool.forEach((key, featureEntity) -> resultMap.put(key, featureEntity.getResult()));
        } else {
            featureEntitiesPool.entrySet().stream().filter(
                    entry -> entry.getValue().getFeatureEnum() != FeatureEnums.ORIGIN_DATA && entry.getValue().getFeatureBean().isOutput()
            ).forEach(entry -> resultMap.put(entry.getKey(), entry.getValue().getResult()));
        }
        return resultMap;
    }

    /**
     * Traces back through the dependency chain to find the root cause of a failure.
     *
     * @param featureEntity the feature entity that experienced the failure
     * @return the name of the feature that is the root cause of the failure
     */
    public String getRootErrorFeature(FeatureEntity featureEntity) {
        FeatureEntity tempFeatureEntity = featureEntity;
        while (tempFeatureEntity.getErrorParent() != null) {
            tempFeatureEntity = featureEntitiesPool.get(tempFeatureEntity.getErrorParent());
        }
        return tempFeatureEntity.getFeatureBean().getName();
    }
}
