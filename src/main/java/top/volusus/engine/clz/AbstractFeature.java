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

package top.volusus.engine.clz;

/**
 * Abstract base class for dynamically generated feature implementations.
 * <p>
 * This class is extended by classes generated at runtime by {@link FeatureClassGenerator}.
 * It holds a reference to the original bean instance that contains the feature method.
 * </p>
 * <p>
 * <b>Usage:</b> This class is not intended to be extended directly. Instead,
 * use the {@code @Feature} annotation to define features, and the engine will
 * automatically generate subclasses.
 * </p>
 *
 * @author zhanghuan
 * @since 2020/01/29
 * @see IFeature
 * @see FeatureClassGenerator
 */
public abstract class AbstractFeature implements IFeature {

    /**
     * Reference to the original Spring bean containing the feature method.
     */
    protected Object bean;

    /**
     * Constructs an AbstractFeature with a reference to the original bean.
     *
     * @param bean the Spring bean instance that contains the feature method
     */
    public AbstractFeature(Object bean) {
        this.bean = bean;
    }
}
