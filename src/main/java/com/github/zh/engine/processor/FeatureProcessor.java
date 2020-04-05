package com.github.zh.engine.processor;

import com.github.zh.engine.annotation.Feature;
import com.github.zh.engine.annotation.FeatureComponent;
import com.github.zh.engine.clz.FeatureClassGenerator;
import com.github.zh.engine.clz.IFeature;
import com.github.zh.engine.co.AbstractFeatureBean;
import com.github.zh.engine.co.bean.NativeFeatureBean;
import com.github.zh.engine.interfaces.FeaturePostProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhanghuan
 * @created 2020/01/27
 */
@Slf4j
@Component
public class FeatureProcessor implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent>{

    @Autowired
    FeatureClassGenerator featureClassGenerator;

    @Autowired(required = false)
    List<FeaturePostProcessor> featurePostProcessorList;

    @Getter
    private ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().getAnnotation(FeatureComponent.class) == null){
            return bean;
        }
        List<Method> featureMethods = Arrays.stream(bean.getClass().getMethods()).filter(it ->
                it.getDeclaredAnnotation(Feature.class) != null
        ).collect(Collectors.toList());
        featureMethods.forEach(it -> {
            Feature feature = it.getDeclaredAnnotation(Feature.class);

            String catalogClassName = bean.getClass().getName();
            try {

                Class<?>[] parameterTypes = it.getParameterTypes();
                Parameter[] parameters = it.getParameters();
                //生产Class
                Class<?> klz = featureClassGenerator.generateClass(
                        parameterTypes, parameters,
                        catalogClassName, beanName,
                        feature.name(), it.getName());
                //实例化
                IFeature featureObject = (IFeature) klz.getDeclaredConstructors()[0].newInstance(bean);

                //构建FeatureBean
                List<String> parents = Arrays.stream(it.getParameters()).map(Parameter::getName).collect(Collectors.toList());
                NativeFeatureBean nativeFeatureBean = NativeFeatureBean.builder()
                        .feature(featureObject)
                        .name(feature.name())
                        .parents(parents)
                        .children(new ArrayList<>())
                        .featureMetaData(feature)
                        .returnType(it.getReturnType())
                        .build();

                for (FeaturePostProcessor featurePostProcessor : featurePostProcessorList) {
                    nativeFeatureBean = featurePostProcessor.postProcessAfterInitializationFeature(nativeFeatureBean, feature.name());
                }

                if(nativeFeatureBean != null) {
                    featureBeanMap.put(feature.name(), nativeFeatureBean);
                }

                log.info("Feature bean construct success : {}", Objects.requireNonNull(nativeFeatureBean).toString());
            } catch (Exception e) {
                log.error("Generate feature error: {} ", feature.name(), e);
            }
        });

        return bean;
    }

    private void constructFeatureBeanChildren(ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap) {
        featureBeanMap.values().forEach(
                featureBean -> {
                    featureBean.getParents().forEach(
                            parent -> {
                                if(featureBeanMap.containsKey(parent)) {
                                    featureBeanMap.get(parent).getChildren().add(featureBean.getName());
                                }
                            }
                    );
                }
        );
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        constructFeatureBeanChildren(featureBeanMap);
    }
}
