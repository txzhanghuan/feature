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

package top.volusus.engine.co;

import top.volusus.engine.enums.FeatureEnums;
import top.volusus.engine.enums.FeatureStates;
import top.volusus.engine.exception.CalculateException;
import top.volusus.engine.tools.CycleAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Builder class responsible for constructing the feature computation DAG (Directed Acyclic Graph).
 * <p>
 * This class handles the construction of the feature dependency graph from feature bean definitions.
 * It is responsible for:
 * <ul>
 *   <li>Initializing origin data as pre-computed feature entities</li>
 *   <li>Initializing native feature entities for requested features</li>
 *   <li>Initializing outer (external) feature entities</li>
 *   <li>Resolving and adding intermediate dependency features</li>
 *   <li>Constructing parent-child relationships between features</li>
 *   <li>Performing cycle detection analysis</li>
 * </ul>
 *
 * <p>
 * <b>Usage:</b> Create an instance with the required dependencies, then call {@link #build()}
 * to construct the DAG. The builder populates the provided feature entities pool.
 * </p>
 * <p>
 * <b>Thread Safety:</b> This class is not thread-safe. Each computation request should
 * create a new builder instance.
 * </p>
 *
 * @author zhanghuan
 * @see FeatureContext
 * @see FeatureEntity
 */
@Slf4j
public class FeatureDAGBuilder {

    private final FeatureContext featureContext;
    private final ConcurrentHashMap<String, FeatureEntity> featureEntitiesPool;
    private final Map<String, AbstractFeatureBean> featureBeanMap;
    private final Map<String, Object> originDataMap;
    private final Set<String> calcFeatures;
    private final Map<String, ? extends AbstractFeatureBean> outerFeatureBeanMap;

    /**
     * Counter for features that need to be calculated.
     */
    private int needCalcFeaturesCount = 0;

    /**
     * Constructs a new FeatureDAGBuilder with the required dependencies.
     *
     * @param featureContext      the parent context for this DAG
     * @param featureEntitiesPool the pool to populate with feature entities
     * @param featureBeanMap      the map of available native feature beans
     * @param originDataMap       the original input data map, may be null
     * @param calcFeatures        the set of feature names to be calculated
     * @param outerFeatureBeanMap the map of external feature beans, may be null
     */
    public FeatureDAGBuilder(FeatureContext featureContext,
                             ConcurrentHashMap<String, FeatureEntity> featureEntitiesPool,
                             Map<String, AbstractFeatureBean> featureBeanMap,
                             Map<String, Object> originDataMap,
                             Set<String> calcFeatures,
                             Map<String, ? extends AbstractFeatureBean> outerFeatureBeanMap) {
        this.featureContext = featureContext;
        this.featureEntitiesPool = featureEntitiesPool;
        this.featureBeanMap = featureBeanMap;
        this.originDataMap = originDataMap;
        this.calcFeatures = calcFeatures;
        this.outerFeatureBeanMap = outerFeatureBeanMap;
    }

    /**
     * Builds the complete DAG structure by performing all initialization steps.
     * <p>
     * Performs the following steps in order:
     * <ol>
     *   <li>Injects origin data as pre-computed feature entities</li>
     *   <li>Injects outer feature entities if provided</li>
     *   <li>Injects native feature entities for requested features</li>
     *   <li>Resolves and adds intermediate dependency features</li>
     *   <li>Reconstructs parent-child relationships (for outer beans)</li>
     *   <li>Performs cycle detection analysis</li>
     * </ol>
     *
     * @return the count of features that need to be calculated
     */
    public int build() {
        // Inject origin data
        initOriginData();

        // Inject outer feature entities if provided
        if (outerFeatureBeanMap != null) {
            initOuterFeatureEntity();
        }

        // Inject native feature entities to be calculated
        initNativeFeatureEntity();

        // Inject dependent feature entities
        putMiddleFeatureEntity();

        // Reconstruct parent-child relationships (required when using outer beans)
        if (outerFeatureBeanMap != null) {
            constructFeatureBeanChildren();
        }

        // Perform cycle analysis
        cycleAnalysis();

        return needCalcFeaturesCount;
    }

    /**
     * Injects origin data as pre-computed feature entities with SUCCESS status.
     * <p>
     * Origin data is treated as features that are already computed, so their
     * status is set to SUCCESS immediately.
     * </p>
     */
    private void initOriginData() {
        if (originDataMap == null) {
            return;
        }
        originDataMap.forEach((key, value) -> {
            FeatureEntity featureEntity = FeatureEntity.builder()
                    .featureContext(featureContext)
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
     * Injects externally provided feature beans as feature entities.
     */
    private void initOuterFeatureEntity() {
        outerFeatureBeanMap.forEach((key, value) -> {
            FeatureEntity featureEntity = FeatureEntity.builder()
                    .featureContext(featureContext)
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
     * Injects native (locally defined) feature entities for the requested features.
     */
    private void initNativeFeatureEntity() {
        calcFeatures.forEach(feature -> {
            if (!featureBeanMap.containsKey(feature)) {
                return;
            }
            FeatureEntity featureEntity = FeatureEntity.builder()
                    .featureContext(featureContext)
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
     * Resolves and injects intermediate feature entities required for dependency resolution.
     * <p>
     * Uses BFS to traverse the dependency graph and add all required intermediate features
     * that are not yet in the pool.
     * </p>
     *
     * @throws CalculateException if a required feature or input parameter is missing
     */
    private void putMiddleFeatureEntity() {
        Queue<String> queue = new LinkedBlockingDeque<>();
        insertToQueue(queue);
        while (!queue.isEmpty()) {
            String currentEntityKey = queue.poll();
            if (!featureBeanMap.containsKey(currentEntityKey) && !featureEntitiesPool.containsKey(currentEntityKey)) {
                throw new CalculateException(String.format("Missing feature or input parameter: %s", currentEntityKey));
            }
            FeatureEntity featureEntity = FeatureEntity.builder()
                    .featureContext(featureContext)
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

    /**
     * Inserts unresolved parent feature names into the processing queue.
     * <p>
     * Scans all feature entities (excluding origin data) and adds any
     * parent features that are not yet in the pool to the queue.
     * </p>
     *
     * @param queue the queue to add unresolved parent feature names to
     */
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

    /**
     * Reconstructs parent-child relationships for all feature entities.
     * <p>
     * This is required when outer feature beans are included, as their
     * children relationships may not be properly set up.
     * </p>
     */
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

    /**
     * Performs cycle detection analysis on the feature dependency graph.
     * <p>
     * Currently disabled but can be enabled to detect circular dependencies
     * in the feature computation graph.
     * </p>
     *
     * @see CycleAnalysis#isCycle(Map)
     */
    private void cycleAnalysis() {
//        if (CycleAnalysis.isCycle(featureEntitiesPool)) {
//            throw new CalculateException("These features entities has a cycle!");
//        }
    }
}
