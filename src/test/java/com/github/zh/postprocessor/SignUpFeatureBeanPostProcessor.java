package com.github.zh.postprocessor;

import com.github.zh.bean.OuterFeatureBean;
import com.github.zh.engine.interfaces.FeatureBeanPostProcessor;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

/**
 * @author ahuan.zh
 * @version 1.0
 * @date 2021/9/17 14:35
 */
@Component
public class SignUpFeatureBeanPostProcessor implements FeatureBeanPostProcessor<OuterFeatureBean> {

    @Override
    public OuterFeatureBean postProcessAfterInitializationFeature(OuterFeatureBean featureBean) throws BeansException {
        System.out.println(featureBean.getName());
//        System.out.println(featureBean.getProperties().get("a"));
        return featureBean;
    }
}
