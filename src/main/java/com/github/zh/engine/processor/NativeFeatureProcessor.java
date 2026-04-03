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
 * Spring BeanPostProcessor that scans for {@link Feature} annotated methods and creates {@link NativeFeatureBean}s.
 * <p>
 * This processor is responsible for:
 * <ul>
 *   <li>Detecting classes annotated with {@link FeatureClass}</li>
 *   <li>Scanning for methods annotated with {@link Feature}</li>
 *   <li>Generating dynamic {@link IFeature} implementations using Javassist</li>
 *   <li>Creating and registering {@link NativeFeatureBean} instances</li>
 *   <li>Building the parent-child dependency graph after all beans are processed</li>
 * </ul>
 * </p>
 * <p>
 * <b>Lifecycle:</b> This processor runs during Spring's bean post-processing phase.
 * The dependency graph is finalized when the application context is refreshed.
 * </p>
 *
 * @author zhanghuan
 * @date 2020/01/27
 * @see Feature
 * @see FeatureClass
 * @see NativeFeatureBean
 * @see FeatureClassGenerator
 */
@Slf4j
@Component
public class NativeFeatureProcessor extends AbstractFeatureProcessor<NativeFeatureBean> implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    /**
     * Generator for creating dynamic IFeature implementations.
     */
    private final FeatureClassGenerator featureClassGenerator = new FeatureClassGenerator();

    /**
     * Post-processes beans to detect and register feature methods.
     * <p>
     * For beans annotated with {@link FeatureClass}, this method scans all methods
     * for {@link Feature} annotations and creates corresponding {@link NativeFeatureBean}s.
     * </p>
     *
     * @param bean     the bean instance to process
     * @param beanName the name of the bean
     * @return the original bean (unmodified)
     * @throws BeansException           if bean processing fails
     * @throws FeatureCreationException if feature generation or bean creation fails
     */
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
                //Generate executable instance of FeatureBean
                IFeature featureObject = getFeature(bean, beanName, it, featureName);

                //Generate NativeFeatureBean
                NativeFeatureBean nativeFeatureBean = constructNativeFeatureBean(featureClass, it, featureName, featureObject);

                //Execute FeatureBeanPostProcessor post-processor
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
     * Builds and populates a {@link NativeFeatureBean} with all required properties.
     * <p>
     * Extracts feature metadata from annotations, determines parent dependencies from
     * method parameters, and collects custom properties from {@link Property} annotations.
     * </p>
     *
     * @param featureClass  the {@link FeatureClass} annotation from the containing class
     * @param method        the annotated method
     * @param featureName   the resolved feature name
     * @param featureObject the generated {@link IFeature} implementation
     * @return a fully configured {@link NativeFeatureBean}
     */
    private NativeFeatureBean constructNativeFeatureBean(FeatureClass featureClass, Method method, String featureName, IFeature featureObject) {

        Feature feature = method.getDeclaredAnnotation(Feature.class);

        //Initialize parent nodes
        List<String> parents = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.toList());

        //Get properties of NativeBean
        Map<String, String> properties = Arrays.stream(method.getDeclaredAnnotationsByType(Property.class)).collect(
                Collectors.toMap(Property::key, Property::value)
        );

        //Build FeatureBean
        return NativeFeatureBean.builder()
                .feature(featureObject)
                .featureClass(featureClass)
                .name(featureName)
                .output(feature.output())
                .parents(parents)
                .children(new ArrayList<>())
                .featureMetaData(feature)
                .returnType(method.getReturnType())
                .properties(properties)
                .build();
    }

    /**
     * Generates a dynamic {@link IFeature} implementation for the annotated method.
     * <p>
     * Uses Javassist to dynamically generate a class that extends {@link AbstractFeature}
     * and delegates to the original method when executed.
     * </p>
     *
     * @param bean     the bean instance containing the feature method
     * @param beanName the Spring bean name
     * @param it       the method annotated with {@link Feature}
     * @param name     the resolved feature name
     * @return a new instance of the dynamically generated {@link IFeature}
     * @throws Exception if class generation or instantiation fails
     */
    private IFeature getFeature(Object bean, String beanName, Method it, String name) throws Exception {
        String catalogClassName = bean.getClass().getName();
        Class<?>[] parameterTypes = it.getParameterTypes();
        Parameter[] parameters = it.getParameters();
        //Generate Class
        Class<?> klz = featureClassGenerator.generateClass(
                parameterTypes, parameters,
                catalogClassName, beanName,
                name, it.getName());
        //Instantiate
        return (IFeature) klz.getDeclaredConstructors()[0].newInstance(bean);
    }

    /**
     * Handles the {@link ContextRefreshedEvent} to finalize the dependency graph.
     * <p>
     * This method is called when the Spring application context is fully initialized,
     * triggering the construction of parent-child relationships between feature beans.
     * </p>
     *
     * @param event the context refreshed event
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        super.fillFeatureBeanChildren();
    }
}
