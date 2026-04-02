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

package com.github.zh.engine.interfaces;

import com.github.zh.engine.co.AbstractFeatureBean;
import org.springframework.beans.BeansException;

/**
 * Interface for customizing feature beans after they are initialized.
 * <p>
 * Implementations of this interface can modify or enhance feature beans
 * during the registration process. This is similar to Spring's
 * {@link org.springframework.beans.factory.config.BeanPostProcessor} but
 * specifically for feature beans.
 * </p>
 * <p>
 * <b>Usage:</b> Create a Spring bean implementing this interface to intercept
 * and modify feature beans of a specific type.
 * </p>
 * <p>
 * <b>Example:</b>
 * <pre>
 * {@code
 * @Component
 * public class MyPostProcessor implements FeatureBeanPostProcessor<NativeFeatureBean> {
 *     @Override
 *     public NativeFeatureBean postProcessAfterInitializationFeature(NativeFeatureBean bean) {
 *         // Custom processing
 *         return bean;
 *     }
 *
 *     @Override
 *     public Class<?> returnClass() {
 *         return NativeFeatureBean.class;
 *     }
 * }
 * }
 * </pre>
 * </p>
 *
 * @param <T> the type of feature bean this processor handles
 * @author zhanghuan
 * Date: 2020/4/5
 * Time: 1:07 PM
 * @see AbstractFeatureBean
 * @see com.github.zh.engine.processor.AbstractFeatureProcessor
 */
public interface FeatureBeanPostProcessor<T extends AbstractFeatureBean> {

    /**
     * Post-processes a feature bean after initialization.
     * <p>
     * Called after the feature bean has been constructed but before it is registered
     * in the feature bean map. Implementations can modify the bean or replace it entirely.
     * </p>
     *
     * @param featureBean the feature bean to process
     * @return the processed feature bean, which may be the original or a replacement
     * @throws BeansException if post-processing fails
     */
    default T postProcessAfterInitializationFeature(T featureBean) throws BeansException {
        return featureBean;
    }

    /**
     * Returns the class type that this processor handles.
     * <p>
     * The processor will only be applied to feature beans that are assignable
     * to the returned class type.
     * </p>
     *
     * @return the class type of feature beans this processor handles
     */
    Class<?> returnClass();

}
