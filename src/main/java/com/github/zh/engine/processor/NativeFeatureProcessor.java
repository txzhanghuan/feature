package com.github.zh.engine.processor;

import com.github.zh.engine.annotation.Feature;
import com.github.zh.engine.annotation.FeatureClass;
import com.github.zh.engine.annotation.properties.Property;
import com.github.zh.engine.clz.FeatureClassGenerator;
import com.github.zh.engine.clz.IFeature;
import com.github.zh.engine.co.bean.NativeFeatureBean;
import com.github.zh.engine.exception.FeatureCreationException;
import com.github.zh.engine.interfaces.FeatureBeanPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhanghuan
 * @created 2020/01/27
 */
@Slf4j
@Component
public class NativeFeatureProcessor extends AbstractFeatureProcessor implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private final FeatureClassGenerator featureClassGenerator = new FeatureClassGenerator();

    @Autowired(required = false)
    private List<FeatureBeanPostProcessor<NativeFeatureBean>> featureBeanPostProcessors;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        FeatureClass featureClass = bean.getClass().getAnnotation(FeatureClass.class);
        if (featureClass == null) {
            return bean;
        }
        List<Method> featureMethods = Arrays.stream(bean.getClass().getMethods()).filter(it ->
                it.getDeclaredAnnotation(Feature.class) != null
        ).collect(Collectors.toList());
        featureMethods.forEach(it -> {
            Feature feature = it.getDeclaredAnnotation(Feature.class);

            String name = "".equals(feature.name()) ? it.getName() : feature.name();
            String catalogClassName = bean.getClass().getName();
            try {

                Class<?>[] parameterTypes = it.getParameterTypes();
                Parameter[] parameters = it.getParameters();
                //生产Class
                Class<?> klz = featureClassGenerator.generateClass(
                        parameterTypes, parameters,
                        catalogClassName, beanName,
                        name, it.getName());
                //实例化
                IFeature featureObject = (IFeature) klz.getDeclaredConstructors()[0].newInstance(bean);

                //初始化父节点
                List<String> parents = Arrays.stream(it.getParameters()).map(Parameter::getName).collect(Collectors.toList());

                //获取NativeBean的Properties
                Map<String, String> properties = Arrays.stream(it.getDeclaredAnnotationsByType(Property.class)).collect(
                        Collectors.toMap(Property::key, Property::value)
                );

                //构建FeatureBean
                NativeFeatureBean nativeFeatureBean = NativeFeatureBean.builder()
                        .feature(featureObject)
                        .featureClass(featureClass)
                        .name(name)
                        .output(feature.output())
                        .parents(parents)
                        .children(new ArrayList<>())
                        .featureMetaData(feature)
                        .returnType(it.getReturnType())
                        .properties(properties)
                        .build();

                //寻找Feature后处理器
                if (!CollectionUtils.isEmpty(featureBeanPostProcessors)) {
                    for (FeatureBeanPostProcessor<NativeFeatureBean> featureBeanPostProcessor : featureBeanPostProcessors) {
                        nativeFeatureBean = featureBeanPostProcessor.postProcessAfterInitializationFeature(nativeFeatureBean);
                    }
                }

                if (nativeFeatureBean != null) {
                    AbstractFeatureProcessor.getFeatureBeanMap().put(name, nativeFeatureBean);
                }

                log.info("Feature bean construct success : {}", Objects.requireNonNull(nativeFeatureBean));
            } catch (Exception e) {
                log.error("Generate feature error: {} ", name, e);
                throw new FeatureCreationException(e.getMessage(), e);
            }
        });

        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        super.constructFeatureBeanChildren();
    }
}
