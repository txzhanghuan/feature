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

package top.volusus.engine.processor;

import top.volusus.engine.co.AbstractFeatureBean;
import top.volusus.engine.interfaces.FeatureBeanPostProcessor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract base class for processing and managing feature beans.
 * <p>
 * This class provides the common infrastructure for feature processors, including:
 * <ul>
 *   <li>A thread-safe map for storing registered feature beans</li>
 *   <li>Post-processing support via {@link FeatureBeanPostProcessor}</li>
 *   <li>Dependency graph construction (parent-child relationships)</li>
 * </ul>
 *
 * <p>
 * <b>Thread Safety:</b> Uses {@link ConcurrentHashMap} for thread-safe feature bean storage.
 * </p>
 *
 * @param <T> the type of feature bean this processor handles, must extend {@link AbstractFeatureBean}
 * @author zhanghuan
 * @version 1.0
 * @since 2021/9/17 14:48
 * @see AbstractFeatureBean
 * @see FeatureBeanPostProcessor
 * @see NativeFeatureProcessor
 */
public abstract class AbstractFeatureProcessor<T extends AbstractFeatureBean> {

    /**
     * Thread-safe map storing all registered feature beans, keyed by feature name.
     */
    @Getter
    private final ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = new ConcurrentHashMap<>();

    /**
     * List of post-processors to apply to feature beans after initialization.
     */
    @Autowired(required = false)
    private List<FeatureBeanPostProcessor<T>> featureBeanPostProcessors;

    /**
     * Populates the children list for each feature bean based on parent-child relationships.
     * <p>
     * This method iterates through all feature beans and adds each feature as a child
     * to its parent features. Should be called after all feature beans are registered.
     * </p>
     */
    protected void fillFeatureBeanChildren() {
        featureBeanMap.values().forEach(
                featureBean -> featureBean.getParents().forEach(
                        parent -> {
                            if (featureBeanMap.containsKey(parent)) {
                                featureBeanMap.get(parent).getChildren().add(featureBean.getName());
                            }
                        }
                )
        );
    }

    /**
     * Registers a feature bean in the feature bean map.
     *
     * @param featureName the unique name of the feature
     * @param featureBean the feature bean to register
     */
    protected void put(String featureName, AbstractFeatureBean featureBean) {
        featureBeanMap.put(featureName, featureBean);
    }

    /**
     * Applies all registered {@link FeatureBeanPostProcessor}s to a feature bean.
     * <p>
     * Each post-processor is invoked in order if its {@code returnClass()} is assignable
     * from the feature bean's class.
     * </p>
     *
     * @param abstractFeatureBean the feature bean to process
     * @return the processed feature bean, which may be modified or replaced by post-processors
     */
    protected T doFeatureBeanPostProcessor(T abstractFeatureBean) {
        if (!CollectionUtils.isEmpty(featureBeanPostProcessors)) {
            for (FeatureBeanPostProcessor<T> featureBeanPostProcessor : featureBeanPostProcessors) {
                if (featureBeanPostProcessor.returnClass().isAssignableFrom(abstractFeatureBean.getClass())) {
                    abstractFeatureBean = featureBeanPostProcessor.postProcessAfterInitializationFeature(abstractFeatureBean);
                }
            }
        }
        return abstractFeatureBean;
    }

}
