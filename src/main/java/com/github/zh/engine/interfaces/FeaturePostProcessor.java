package com.github.zh.engine.interfaces;

import com.github.zh.engine.co.AbstractFeatureBean;
import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * @author 阿桓
 * Date: 2020/4/5
 * Time: 1:07 下午
 * Description:
 */
public interface FeaturePostProcessor {

    @Nullable
    default <T extends AbstractFeatureBean> T postProcessAfterInitializationFeature(T featureBean, String featureBeanName) throws BeansException {
        return featureBean;
    }
}
