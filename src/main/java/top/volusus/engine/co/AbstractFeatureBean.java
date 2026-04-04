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

package top.volusus.engine.co;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Abstract base class for all feature beans in the Feature Engine.
 * <p>
 * A feature bean encapsulates the metadata and execution logic for a single feature
 * in the computation graph. This class provides common properties shared by all
 * feature bean implementations.
 * </p>
 * <p>
 * <b>Properties:</b>
 * <ul>
 *   <li>{@code name} - Unique identifier for the feature</li>
 *   <li>{@code output} - Whether this feature should be included in the final result</li>
 *   <li>{@code parents} - List of feature names this feature depends on (inputs)</li>
 *   <li>{@code children} - List of feature names that depend on this feature</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b> This class is not thread-safe by design. Feature beans are typically
 * created during application initialization and remain immutable afterward.
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/3/26
 * Time: 10:24 AM
 * @see IFeatureBean
 * @see top.volusus.engine.co.bean.NativeFeatureBean
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractFeatureBean implements IFeatureBean {

    /**
     * The unique name identifier for this feature.
     */
    protected String name;

    /**
     * Whether this feature's result should be included in the final output.
     * <p>
     * When set to {@code true}, the feature value will be returned in the calculation result.
     * When {@code false}, the feature serves only as an intermediate computation.
     * </p>
     */
    protected boolean output;

    /**
     * List of parent feature names that this feature depends on.
     * <p>
     * These represent the input parameters required for this feature's computation.
     * </p>
     */
    protected List<String> parents;

    /**
     * List of child feature names that depend on this feature.
     * <p>
     * These are automatically populated during context initialization.
     * </p>
     */
    protected List<String> children;
}
