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

package com.github.zh.engine.annotation;

import java.lang.annotation.*;

/**
 * Annotation to mark a method as a feature computation function.
 * <p>
 * Methods annotated with {@code @Feature} will be automatically discovered
 * by {@link com.github.zh.engine.processor.NativeFeatureProcessor} and registered
 * as feature beans in the computation engine.
 * </p>
 * <p>
 * <b>Requirements:</b>
 * <ul>
 *   <li>The containing class must be annotated with {@link FeatureClass}</li>
 *   <li>The method must be public</li>
 *   <li>Method parameters define the dependencies (parent features)</li>
 * </ul>
 * </p>
 * <p>
 * <b>Example:</b>
 * <pre>
 * {@code
 * @FeatureClass
 * @Component
 * public class MyFeatures {
 *     @Feature(name = "sumFeature", output = true)
 *     public Integer sum(Integer a, Integer b) {
 *         return a + b;
 *     }
 * }
 * }
 * </pre>
 * </p>
 *
 * @author zhanghuan
 * @date 2020/01/27
 * @see FeatureClass
 * @see com.github.zh.engine.processor.NativeFeatureProcessor
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Feature {
    /**
     * The unique name of the feature.
     * <p>
     * If not specified, defaults to the method name.
     * </p>
     *
     * @return the feature name, or empty string to use method name
     */
    String name() default "";

    /**
     * Whether this feature's result should be included in the final output.
     * <p>
     * When set to {@code true}, the computed value will be returned in the calculation result.
     * When {@code false}, the feature serves only as an intermediate computation.
     * </p>
     *
     * @return true if the feature should be in output; false for intermediate-only features
     */
    boolean output() default true;
}
