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
 * Annotation to mark a class as a container for feature methods.
 * <p>
 * Classes annotated with {@code @FeatureClass} are scanned during application startup
 * by {@link com.github.zh.engine.processor.NativeFeatureProcessor} for methods
 * annotated with {@link Feature}.
 * </p>
 * <p>
 * <b>Requirements:</b>
 * <ul>
 *   <li>The class should be a Spring-managed bean (annotated with {@code @Component} or similar)</li>
 *   <li>Feature methods within must be annotated with {@link Feature}</li>
 * </ul>
 * </p>
 * <p>
 * <b>Example:</b>
 * <pre>
 * {@code
 * @FeatureClass(description = "Mathematical operations")
 * @Component
 * public class MathFeatures {
 *     @Feature
 *     public Integer add(Integer a, Integer b) {
 *         return a + b;
 *     }
 * }
 * }
 * </pre>
 * </p>
 *
 * @author zhanghuan
 * @date 2020/01/27
 * @see Feature
 * @see com.github.zh.engine.processor.NativeFeatureProcessor
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface FeatureClass {

    /**
     * Optional description of the feature class.
     * <p>
     * Used for documentation purposes to describe the purpose or category
     * of features contained within this class.
     * </p>
     *
     * @return the description of this feature class
     */
    String description() default "";
}
