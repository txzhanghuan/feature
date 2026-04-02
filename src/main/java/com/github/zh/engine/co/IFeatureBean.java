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

package com.github.zh.engine.co;

/**
 * Functional interface for executing feature computations.
 * <p>
 * This interface defines the core execution contract for all feature beans.
 * Implementations receive an array of computed parent values and return
 * the computed result for this feature.
 * </p>
 * <p>
 * <b>Usage:</b> This interface is typically implemented by {@link AbstractFeatureBean}
 * subclasses or dynamically generated classes.
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/3/26
 * Time: 9:50 AM
 * @see AbstractFeatureBean
 */
@FunctionalInterface
public interface IFeatureBean {

    /**
     * Executes the feature computation with the given input arguments.
     * <p>
     * The arguments are provided in the same order as the parent features
     * defined for this feature bean.
     * </p>
     *
     * @param args an array of computed values from parent features, in dependency order
     * @return the computed result of this feature, which may be of any type
     */
    Object execute(Object[] args);
}
