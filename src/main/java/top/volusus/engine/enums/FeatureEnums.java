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

package top.volusus.engine.enums;

/**
 * Enumeration of feature types in the computation engine.
 * <p>
 * This enum categorizes different types of features based on their origin:
 * <ul>
 *   <li>{@link #ORIGIN_DATA} - Raw input data provided at calculation time</li>
 *   <li>{@link #NATIVE_FEATURE} - Features defined locally via {@code @Feature} annotation</li>
 *   <li>{@link #OUTER_FEATURE} - Features provided externally at runtime</li>
 * </ul>
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/3/26
 * Time: 10:00 AM
 * @see top.volusus.engine.co.FeatureEntity
 */
public enum FeatureEnums {

    /**
     * External input data provided at calculation time.
     * <p>
     * These are pre-computed values passed to the engine via the originDataMap parameter.
     * They have no parents and are immediately marked as SUCCESS.
     * </p>
     */
    ORIGIN_DATA,

    /**
     * Feature defined locally using the {@code @Feature} annotation.
     * <p>
     * These features are discovered and registered during application startup
     * by {@link top.volusus.engine.processor.NativeFeatureProcessor}.
     * </p>
     */
    NATIVE_FEATURE,

    /**
     * Feature provided externally at runtime via calcWithOuterFeatureBean.
     * <p>
     * These features allow dynamic injection of computation logic that
     * is not defined at application startup.
     * </p>
     */
    OUTER_FEATURE
}
