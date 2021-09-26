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
 * @author 阿桓
 * Date: 2020/3/23
 * Time: 8:30 下午
 * Description: Feature的实例
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

    public void execute(Map<String, String> logContext) {

        if (logContext != null) {
            MDC.setContextMap(logContext);
        }

        //判断是否初始状态
        if (!status.get().equals(FeatureStates.INIT)) {
            MDC.clear();
            return;
        }

        //检查Context是否已经失败
        if (this.featureContext.isFastFail()) {
            if (status.compareAndSet(FeatureStates.INIT, FeatureStates.FAILED)) {
                log.debug("Feature: {}， fast failed.", featureBean.getName());
                this.featureContext.getCountDownLatch().countDown();

                MDC.clear();
                //通知子节点快速失败
                notifyChildren(logContext);
                return;
            }
        }

        //检查参数是否都已计算完成
        if (!checkParamsReady()) {
            MDC.clear();
            return;
        }

        //判断参数属否出错
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

        //计算
        //初始化 -> 计算中
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
                log.debug("Thread：{}, feature: {}, result: {}. complete, cost(ms)：{}", Thread.currentThread().getName(), this.getFeatureBean().getName(), result, stopWatch.getTotalTimeMillis());
            } catch (Exception e) {
                log.error("Feature: {}, failed. input param：{}", featureBean.getName(), args, e);
                error = e;
                status.set(FeatureStates.FAILED);
                this.featureContext.setFastFail(true);
            } finally {
                this.featureContext.getCountDownLatch().countDown();
            }
        }

        MDC.clear();

        //通知子节点计算
        notifyChildren(logContext);

    }

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

    private boolean checkParamsReady() {
        return parents.stream().allMatch(
                it -> featureContext.getFeatureEntitiesPool().get(it).status.get().isEndStates()
        );
    }

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
