package com.github.zh.engine.co;

import com.github.zh.engine.enums.FeatureEnums;
import com.github.zh.engine.enums.FeatureStates;
import com.github.zh.engine.exception.CalculateException;
import com.github.zh.engine.tools.CycleAnalysis;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author 阿桓
 * Date: 2020/3/20
 * Time: 3:09 下午
 * Description: 一次请求的上下文
 */
@Slf4j
@Component

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
     * 需要计算的变量数
     */
    private int needCalcFeaturesCount = 0;

    /**
     * 执行变量计算
     *
     * @param logContext 日志上下文
     * @throws InterruptedException
     */
    public void executeAll(long timeout, Map<String, String> logContext) throws InterruptedException {
        if (needCalcFeaturesCount == 0) {
            throw new CalculateException("无变量需计算");
        }
        featureEntitiesPool.values().forEach(
                featureEntity -> CompletableFuture.runAsync(
                        () -> featureEntity.execute(logContext)
                        , pool)
        );
        if (!this.countDownLatch.await(timeout, TimeUnit.MILLISECONDS)) {
            throw new CalculateException("计算超时");
        }
    }

    /**
     * 初始化计算上下文（本地计算）
     *
     * @param pool
     * @param originDataMap
     * @param calcFeatures
     * @param featureBeanMap
     */
    public void init(ThreadPoolExecutor pool, Map<String, Object> originDataMap, Set<String> calcFeatures, Map<String, AbstractFeatureBean> featureBeanMap) {

        //指定计算线程池
        this.pool = pool;

        //注入原始数据
        initOriginData(originDataMap);

        //注入需要计算的NativeFeatureEntity
        initNativeFeatureEntity(calcFeatures, featureBeanMap);

        //注入计算所依赖的FeatureEntity
        putMiddleFeatureEntity(featureBeanMap);

        //环分析
        cycleAnalysis();

        //初始化需要计算的变量个数
        initCountDownLatch();
    }

    /**
     * 初始化计算上下文（带入外部计算FeatureBean）
     *
     * @param pool
     * @param originDataMap
     * @param calcFeatures
     * @param featureBeanMap
     */
    public void initWithOuterFeatureBean(ThreadPoolExecutor pool, Map<String, Object> originDataMap, Set<String> calcFeatures,
                                         Map<String, AbstractFeatureBean> featureBeanMap,
                                         Map<String, ? extends AbstractFeatureBean> outerFeatureBeanMap) {

        //指定计算线程池
        this.pool = pool;

        //注入原始数据
        initOriginData(originDataMap);

        //注入外部所需计算的FeatureEntity
        initOuterFeatureEntity(outerFeatureBeanMap);

        //注入需要计算的NativeFeatureEntity
        initNativeFeatureEntity(calcFeatures, featureBeanMap);

        //注入计算所依赖的FeatureEntity
        putMiddleFeatureEntity(featureBeanMap);

        //重新构建依赖关系
        constructFeatureBeanChildren();

        //环分析
        cycleAnalysis();

        //初始化需要计算的变量个数
        initCountDownLatch();

    }

    /**
     * 环分析
     */
    private void cycleAnalysis() {

        if (CycleAnalysis.isCycle(featureEntitiesPool)) {
            throw new CalculateException("These features entities has a cycle!");
        }

    }

    private void constructFeatureBeanChildren() {
        featureEntitiesPool.forEach(
                (key, featureEntity) -> featureEntity.getParents().forEach(
                        parent -> {
                            if (featureEntitiesPool.containsKey(parent) && !featureEntitiesPool.get(parent).getChildren().contains(key)) {
                                featureEntitiesPool.get(parent).getChildren().add(key);
                            }
                        }
                )
        );
    }

    private void initCountDownLatch() {
        this.countDownLatch = new CountDownLatch(needCalcFeaturesCount);
    }

    /**
     * 注入原始数据
     *
     * @param originDataMap
     */
    private void initOriginData(Map<String, Object> originDataMap) {
        if (originDataMap == null) {
            return;
        }
        originDataMap.forEach((key, value) -> {
            FeatureEntity featureEntity = FeatureEntity.builder()
                    .featureContext(this)
                    .parents(new ArrayList<>())
                    .children(new ArrayList<>())
                    .featureEnum(FeatureEnums.ORIGIN_DATA)
                    .status(new AtomicReference<>(FeatureStates.SUCCESS))
                    .result(value)
                    .featureBean(null)
                    .build();
            featureEntitiesPool.putIfAbsent(key, featureEntity);
        });
    }

    /**
     * 注入外部所需计算的FeatureEntity
     *
     * @param outerFeatureBeanMap
     */
    private void initOuterFeatureEntity(Map<String, ? extends AbstractFeatureBean> outerFeatureBeanMap) {
        outerFeatureBeanMap.forEach((key, value) -> {
            FeatureEntity featureEntity = FeatureEntity.builder()
                    .featureContext(this)
                    .parents(CollectionUtils.isEmpty(value.parents) ? new ArrayList<>() : new ArrayList<>(value.parents))
                    .children(CollectionUtils.isEmpty(value.children) ? new ArrayList<>() : new ArrayList<>(value.children))
                    .featureEnum(FeatureEnums.OUTER_FEATURE)
                    .featureBean(value)
                    .build();
            if (!featureEntitiesPool.containsKey(key)) {
                featureEntitiesPool.put(key, featureEntity);
                needCalcFeaturesCount++;
            }
        });
    }

    /**
     * 注入需要计算的NativeFeatureEntity
     *
     * @param calcFeatures
     * @param featureBeanMap
     */
    private void initNativeFeatureEntity(Set<String> calcFeatures, Map<String, AbstractFeatureBean> featureBeanMap) {
        calcFeatures.forEach(feature -> {
            if (!featureBeanMap.containsKey(feature)) {
                return;
            }
            FeatureEntity featureEntity = FeatureEntity.builder()
                    .featureContext(this)
                    .parents(CollectionUtils.isEmpty(featureBeanMap.get(feature).parents) ? new ArrayList<>() : new ArrayList<>(featureBeanMap.get(feature).parents))
                    .children(CollectionUtils.isEmpty(featureBeanMap.get(feature).children) ? new ArrayList<>() : new ArrayList<>(featureBeanMap.get(feature).children))
                    .featureEnum(FeatureEnums.NATIVE_FEATURE)
                    .featureBean(featureBeanMap.get(feature))
                    .build();
            if (!featureEntitiesPool.containsKey(feature)) {
                featureEntitiesPool.put(feature, featureEntity);
                needCalcFeaturesCount++;
            }
        });
    }

    /**
     * 注入需要计算的中间FeatureEntity
     *
     * @param featureBeanMap
     */
    private void putMiddleFeatureEntity(Map<String, AbstractFeatureBean> featureBeanMap) {
        Queue<String> queue = new LinkedBlockingDeque<>();
        insertToQueue(queue);
        while (!queue.isEmpty()) {
            String currentEntityKey = queue.poll();
            if (!featureBeanMap.containsKey(currentEntityKey) && !featureEntitiesPool.containsKey(currentEntityKey)) {
                throw new CalculateException(String.format("缺少Feature或者输入参数: %s", currentEntityKey));
            }
            FeatureEntity featureEntity = FeatureEntity.builder()
                    .featureContext(this)
                    .parents(new ArrayList<>(featureBeanMap.get(currentEntityKey).parents))
                    .children(new ArrayList<>(featureBeanMap.get(currentEntityKey).children))
                    .featureEnum(FeatureEnums.NATIVE_FEATURE)
                    .featureBean(featureBeanMap.get(currentEntityKey))
                    .build();
            if (!featureEntitiesPool.containsKey(currentEntityKey)) {
                featureEntitiesPool.put(currentEntityKey, featureEntity);
                needCalcFeaturesCount++;
            }
            insertToQueue(queue);
        }
    }

    private void insertToQueue(Queue<String> queue) {
        featureEntitiesPool.values().stream().filter(
                it -> !it.getFeatureEnum().equals(FeatureEnums.ORIGIN_DATA)
        ).forEach(
                it -> it.getParents().stream().filter(
                        tempParent -> !featureEntitiesPool.containsKey(tempParent)
                ).forEach(
                        queue::offer
                )
        );
    }

    private void checkFail() {
        if (fastFail) {
            FeatureEntity featureEntity = featureEntitiesPool.values().stream().filter(
                    it -> it.getStatus().get().equals(FeatureStates.FAILED)
                            && it.getError() != null).findFirst().get();
            String errorFeature = getRootErrorFeature(featureEntity);
            log.error("Calculate failed! The root failed feature is {}", errorFeature, featureEntitiesPool.get(errorFeature).getError());
            throw new CalculateException(featureEntitiesPool.get(errorFeature).getError());
        }
    }

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

    public String getRootErrorFeature(FeatureEntity featureEntity) {
        FeatureEntity tempFeatureEntity = featureEntity;
        while (tempFeatureEntity.getErrorParent() != null) {
            tempFeatureEntity = featureEntitiesPool.get(tempFeatureEntity.getErrorParent());
        }
        return tempFeatureEntity.getFeatureBean().getName();
    }
}
