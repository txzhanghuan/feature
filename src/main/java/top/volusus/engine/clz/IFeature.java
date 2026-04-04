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
 * Interface for executable feature implementations.
 * <p>
 * This interface defines the execution contract for dynamically generated
 * feature classes. Implementations are created by {@link FeatureClassGenerator}
 * and wrap the actual {@code @Feature} annotated methods.
 * </p>
 * <p>
 * <b>Note:</b> This is different from {@link top.volusus.engine.co.IFeatureBean}
 * which is the higher-level feature bean interface. This interface is specifically
 * for the dynamically generated execution wrappers.
 * </p>
 *
 * @author zhanghuan
 * @since 2020/01/27
 * @see AbstractFeature
 * @see FeatureClassGenerator
 * @see top.volusus.engine.co.bean.NativeFeatureBean
 */
public interface IFeature {

    /**
     * Executes the wrapped feature method with the given arguments.
     * <p>
     * The arguments are passed as an Object array and cast to the appropriate
     * types by the generated implementation before calling the actual method.
     * </p>
     *
     * @param args the input arguments from parent features, in order
     * @return the result of the feature computation
     */
    Object execute(Object[] args);
}
