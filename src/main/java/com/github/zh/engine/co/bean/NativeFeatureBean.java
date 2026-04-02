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

package com.github.zh.engine.co.bean;

import com.github.zh.engine.annotation.Feature;
import com.github.zh.engine.annotation.FeatureClass;
import com.github.zh.engine.clz.IFeature;
import com.github.zh.engine.co.AbstractFeatureBean;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a natively defined feature bean created from annotated methods.
 * <p>
 * This class extends {@link AbstractFeatureBean} to provide metadata and execution
 * capabilities for features defined using the {@link Feature} annotation. It holds
 * references to the original annotation metadata, the dynamically generated
 * {@link IFeature} implementation, and custom properties.
 * </p>
 * <p>
 * <b>Creation:</b> Instances are created by {@link com.github.zh.engine.processor.NativeFeatureProcessor}
 * during application startup when scanning for {@code @Feature} annotated methods.
 * </p>
 * <p>
 * <b>Execution:</b> The {@link #execute(Object[])} method delegates to the dynamically
 * generated {@link IFeature} instance which invokes the original annotated method.
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/3/20
 * Time: 11:48 AM
 * @see Feature
 * @see FeatureClass
 * @see AbstractFeatureBean
 * @see IFeature
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class NativeFeatureBean extends AbstractFeatureBean {

    /**
     * The {@link Feature} annotation metadata from the original method.
     */
    private Feature featureMetaData;

    /**
     * The {@link FeatureClass} annotation from the containing class.
     */
    private FeatureClass featureClass;

    /**
     * The dynamically generated {@link IFeature} implementation that invokes the original method.
     */
    private IFeature feature;

    /**
     * The return type of the original annotated method.
     */
    private Class<?> returnType;

    /**
     * Custom properties defined via {@link com.github.zh.engine.annotation.properties.Property} annotations.
     */
    private Map<String, String> properties;

    /**
     * Constructs a new NativeFeatureBean with all required properties.
     *
     * @param name            the unique feature name
     * @param output          whether this feature should be included in output
     * @param parents         list of parent feature names (dependencies)
     * @param children        list of child feature names (dependents)
     * @param featureMetaData the {@link Feature} annotation metadata
     * @param featureClass    the {@link FeatureClass} annotation metadata
     * @param feature         the dynamically generated IFeature implementation
     * @param returnType      the return type of the original method
     * @param properties      custom properties from Property annotations
     */
    @Builder
    public NativeFeatureBean(String name, boolean output, List<String> parents,
                             List<String> children, Feature featureMetaData, FeatureClass featureClass, IFeature feature,
                             Class<?> returnType, Map<String, String> properties) {
        super(name, output, parents, children);
        this.featureMetaData = featureMetaData;
        this.feature = feature;
        this.featureClass = featureClass;
        this.returnType = returnType;
        this.properties = properties;
    }

    /**
     * Executes the feature computation by delegating to the dynamically generated IFeature.
     *
     * @param args an array of computed values from parent features
     * @return the computed result from the original annotated method
     */
    @Override
    public Object execute(Object[] args) {
        return feature.execute(args);
    }
}
