package com.github.zh.postprocessor;

import com.github.zh.engine.co.bean.NativeFeatureBean;
import com.github.zh.engine.interfaces.FeatureBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

/**
 * @author ahuan.zh
 * @version 1.0
 * @date 2021/9/17 14:35
 */
@Component
public class SignUpFeatureBeanPostProcessor implements FeatureBeanPostProcessor<NativeFeatureBean> {

    @Override
    public NativeFeatureBean postProcessAfterInitializationFeature(NativeFeatureBean featureBean) throws BeansException {
        System.out.println(featureBean.getName());
        System.out.println(featureBean.getProperties().toString());
        return featureBean;
    }
}
