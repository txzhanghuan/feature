package com.github.zh.engine.interfaces;

import com.github.zh.engine.co.AbstractFeatureBean;
import org.springframework.beans.BeansException;

/**
 * @author 阿桓
 * Date: 2020/4/5
 * Time: 1:07 下午
 * Description:
 */
public interface FeatureBeanPostProcessor<T extends AbstractFeatureBean> {

    default T postProcessAfterInitializationFeature(T featureBean) throws BeansException {
        return featureBean;
    }

//    default Class<T> getTClass() {
//        Class<T> tClass = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        return tClass;
//    }
}
