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
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents an instance of a feature during computation.
 * <p>
 * This class encapsulates the runtime state of a feature including its dependencies (parents),
 * dependents (children), computation status, result, and any error that occurred.
 * </p>
 * <p>
 * <b>Execution Model:</b> Each FeatureEntity executes asynchronously when all its parent
 * dependencies have completed. Upon completion, it notifies its children to trigger their
 * execution. This creates a wave-like parallel execution pattern across the DAG.
 * </p>
 * <p>
 * <b>Thread Safety:</b> Uses {@link AtomicReference} for status management and volatile
 * fields for result and error to ensure thread-safe state transitions.
 * </p>
 * <p>
 * <b>Fast-Fail Support:</b> When any feature fails, the context enters fast-fail mode,
 * causing all pending features to fail immediately without execution.
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/3/23
 * Time: 8:30 PM
 * @see FeatureContext
 * @see AbstractFeatureBean
 * @see FeatureStates
 */
@Data
@Builder
@Slf4j
public class FeatureEntity {

    private FeatureEnums featureEnum;

    private AbstractFeatureBean featureBean;

    private FeatureContext featureContext;

    private List<String> parents;

    private List<String> children;

    @Builder.Default
    private volatile AtomicReference<FeatureStates> status = new AtomicReference<>(FeatureStates.INIT);

    private volatile Object result;

    private volatile Throwable error;

    private volatile String errorParent;

    /**
     * Executes the feature computation.
     * <p>
     * This method handles the complete lifecycle of a feature execution:
     * <ol>
     *   <li>Sets up MDC logging context</li>
     *   <li>Checks for fast-fail condition</li>
     *   <li>Waits for all parent dependencies to complete</li>
     *   <li>Checks for errors in parent features</li>
     *   <li>Executes the actual computation</li>
     *   <li>Notifies children features upon completion</li>
     * </ol>
     * </p>
     * <p>
     * <b>State Transitions:</b> INIT → PROCESSING → SUCCESS/FAILED
     * </p>
     *
     * @param logContext the MDC log context to propagate, may be null
     * @see FeatureStates
     */
    public void execute(Map<String, String> logContext) {

        if (logContext != null) {
            MDC.setContextMap(logContext);
        }

        //Check if it's in initial state
        if (!status.get().equals(FeatureStates.INIT)) {
            MDC.clear();
            return;
        }

        //Check if Context has already failed
        if (this.featureContext.isFastFail()) {
            if (status.compareAndSet(FeatureStates.INIT, FeatureStates.FAILED)) {
                log.debug("Feature: {}, fast failed.", featureBean.getName());
                this.featureContext.getCountDownLatch().countDown();

                MDC.clear();
                //Notify children nodes to fast fail
                notifyChildren(logContext);
                return;
            }
        }

        //Check if all parameters have been calculated
        if (!checkParamsReady()) {
            MDC.clear();
            return;
        }

        //Check if any parameter has an error
        String errorParam = getParamError();
        if (errorParam != null) {
            if (status.compareAndSet(FeatureStates.INIT, FeatureStates.FAILED)) {
                this.errorParent = errorParam;
                error = featureContext.getFeatureEntitiesPool().get(errorParam).getError();
                log.error("Feature: {}, calculate failed. Error param is: {}, the root error param is:{}", featureBean.getName(), errorParam, this.getFeatureContext().getRootErrorFeature(this));
                this.featureContext.setFastFail(true);
                this.featureContext.getCountDownLatch().countDown();
            }
        }

        //Calculate
        //INIT -> PROCESSING
        if (status.compareAndSet(FeatureStates.INIT, FeatureStates.PROCESSING)) {
            List<Object> args = new ArrayList<>();
            parents.forEach(
                    it -> {
                        FeatureEntity tempFeatureEntity = featureContext.getFeatureEntitiesPool().get(it);
                        args.add(tempFeatureEntity.getResult());
                    }
            );
            try {
                StopWatch stopWatch = new StopWatch(featureBean.getName());
                stopWatch.start();
                result = featureBean.execute(args.toArray());
                stopWatch.stop();
                status.set(FeatureStates.SUCCESS);
                log.debug("Thread: {}, feature: {}, result: {}. complete, cost(ms): {}", Thread.currentThread().getName(), this.getFeatureBean().getName(), result, stopWatch.getTotalTimeMillis());
            } catch (Exception e) {
                log.error("Feature: {}, failed. input parameters: {}", featureBean.getName(), args, e);
                error = e;
                status.set(FeatureStates.FAILED);
                this.featureContext.setFastFail(true);
            } finally {
                this.featureContext.getCountDownLatch().countDown();
            }
        }

        MDC.clear();

        //Notify children nodes to calculate
        notifyChildren(logContext);

    }

    /**
     * Notifies all child features that this feature has completed.
     * <p>
     * Submits each child feature for async execution in the thread pool.
     * </p>
     *
     * @param logContext the MDC log context to propagate to child executions
     */
    private void notifyChildren(Map<String, String> logContext) {
        children.forEach(
                it -> {
                    if (featureContext.getFeatureEntitiesPool().containsKey(it)) {
                        FeatureEntity featureEntity = featureContext.getFeatureEntitiesPool().get(it);
                        CompletableFuture.runAsync(() -> featureEntity.execute(logContext), this.featureContext.getPool());
                    }
                }
        );
    }

    /**
     * Checks whether all parent features have completed (either SUCCESS or FAILED).
     *
     * @return {@code true} if all parent features have reached an end state; {@code false} otherwise
     */
    private boolean checkParamsReady() {
        return parents.stream().allMatch(
                it -> featureContext.getFeatureEntitiesPool().get(it).status.get().isEndStates()
        );
    }

    /**
     * Checks if any parent feature has failed and returns the name of the failed parent.
     *
     * @return the name of the first failed parent feature, or null if no parent has failed
     */
    private String getParamError() {
        boolean isError = parents.stream().anyMatch(
                it -> featureContext.getFeatureEntitiesPool().get(it).status.get().equals(FeatureStates.FAILED)
        );
        if (isError) {
            return parents.stream().filter(
                    it -> featureContext.getFeatureEntitiesPool().get(it).status.get().equals(FeatureStates.FAILED)
            ).findFirst().get();
        }
        return null;
    }

}
