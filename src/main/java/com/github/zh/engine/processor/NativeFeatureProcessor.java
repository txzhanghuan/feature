package com.github.zh.engine.processor;

import com.github.zh.engine.annotation.Feature;
import com.github.zh.engine.annotation.FeatureClass;
import com.github.zh.engine.annotation.properties.Property;
import com.github.zh.engine.clz.FeatureClassGenerator;
import com.github.zh.engine.clz.IFeature;
import com.github.zh.engine.co.bean.NativeFeatureBean;
import com.github.zh.engine.exception.FeatureCreationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhanghuan
 * @date 2020/01/27
 */
@Slf4j
@Component
public class NativeFeatureProcessor extends AbstractFeatureProcessor<NativeFeatureBean> implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private final FeatureClassGenerator featureClassGenerator = new FeatureClassGenerator();

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

            String featureName = "".equals(feature.name()) ? it.getName() : feature.name();

            try {
                //生成FeatureBean的执行实例
                IFeature featureObject = getFeature(bean, beanName, it, featureName);

                //生成NativeFeatureBean
                NativeFeatureBean nativeFeatureBean = constructNativeFeatureBean(featureClass, it, featureName, featureObject);

                //执行FeatureBeanPostProcessor后置处理器
                nativeFeatureBean = super.doFeatureBeanPostProcessor(nativeFeatureBean);

                super.put(featureName, nativeFeatureBean);

                log.info("Feature bean construct success : {}", Objects.requireNonNull(nativeFeatureBean));
            } catch (Exception e) {
                log.error("Generate feature error: {} ", featureName, e);
                throw new FeatureCreationException(e.getMessage(), e);
            }
        });

        return bean;
    }

    /**
     * 构建填充NativeFeatureBean属性
     *
     * @param featureClass
     * @param it
     * @param featureName
     * @param featureObject
     * @return
     */
    private NativeFeatureBean constructNativeFeatureBean(FeatureClass featureClass, Method it, String featureName, IFeature featureObject) {

        Feature feature = it.getDeclaredAnnotation(Feature.class);

        //初始化父节点
        List<String> parents = Arrays.stream(it.getParameters()).map(Parameter::getName).collect(Collectors.toList());

        //获取NativeBean的Properties
        Map<String, String> properties = Arrays.stream(it.getDeclaredAnnotationsByType(Property.class)).collect(
                Collectors.toMap(Property::key, Property::value)
        );

        //构建FeatureBean
        return NativeFeatureBean.builder()
                .feature(featureObject)
                .featureClass(featureClass)
                .name(featureName)
                .output(feature.output())
                .parents(parents)
                .children(new ArrayList<>())
                .featureMetaData(feature)
                .returnType(it.getReturnType())
                .properties(properties)
                .build();
    }

    /**
     * 生成FeatureBean的执行实例
     *
     * @param bean
     * @param beanName
     * @param it
     * @param name
     * @return
     * @throws Exception
     */
    private IFeature getFeature(Object bean, String beanName, Method it, String name) throws Exception {
        String catalogClassName = bean.getClass().getName();
        Class<?>[] parameterTypes = it.getParameterTypes();
        Parameter[] parameters = it.getParameters();
        //生产Class
        Class<?> klz = featureClassGenerator.generateClass(
                parameterTypes, parameters,
                catalogClassName, beanName,
                name, it.getName());
        //实例化
        return (IFeature) klz.getDeclaredConstructors()[0].newInstance(bean);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        super.fillFeatureBeanChildren();
    }
}
