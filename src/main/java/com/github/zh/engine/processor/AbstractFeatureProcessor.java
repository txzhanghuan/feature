package com.github.zh.engine.processor;

import com.github.zh.engine.co.AbstractFeatureBean;
import com.github.zh.engine.interfaces.FeatureBeanPostProcessor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zhanghuan
 * @version 1.0
 * @date 2021/9/17 14:48
 */
public abstract class AbstractFeatureProcessor<T extends AbstractFeatureBean> {

    @Getter
    private final ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private List<FeatureBeanPostProcessor<T>> featureBeanPostProcessors;

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

    protected void put(String featureName, AbstractFeatureBean featureBean) {
        featureBeanMap.put(featureName, featureBean);
    }

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
