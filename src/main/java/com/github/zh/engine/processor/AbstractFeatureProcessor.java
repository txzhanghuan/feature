package com.github.zh.engine.processor;

import com.github.zh.engine.co.AbstractFeatureBean;
import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ahuan.zh
 * @version 1.0
 * @date 2021/9/17 14:48
 */
public abstract class AbstractFeatureProcessor {

    @Getter
    private final static ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = new ConcurrentHashMap<>();

    protected void constructFeatureBeanChildren() {
        featureBeanMap.values().forEach(
                featureBean -> {
                    featureBean.getParents().forEach(
                            parent -> {
                                if (featureBeanMap.containsKey(parent)) {
                                    featureBeanMap.get(parent).getChildren().add(featureBean.getName());
                                }
                            }
                    );
                }
        );
    }
}
