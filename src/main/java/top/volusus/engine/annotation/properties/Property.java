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

package top.volusus.engine.annotation.properties;

import java.lang.annotation.*;

/**
 * Annotation to define custom key-value properties for a feature method.
 * <p>
 * Properties can be attached to feature methods to provide additional metadata
 * that can be accessed at runtime. This is useful for configuration, documentation,
 * or custom processing logic.
 * </p>
 * <p>
 * This annotation is repeatable, allowing multiple properties to be defined
 * on a single method.
 * </p>
 * <p>
 * <b>Example:</b>
 * <pre>
 * {@code
 * @Feature
 * @Property(key = "category", value = "math")
 * @Property(key = "complexity", value = "O(n)")
 * public Integer compute(Integer input) {
 *     // ...
 * }
 * }
 * </pre>
 *
 * @author zhanghuan
 * @version 1.0
 * @since 2021/9/17 11:41
 * @see Properties
 * @see top.volusus.engine.annotation.Feature
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Repeatable(Properties.class)
public @interface Property {

    /**
     * The property key.
     *
     * @return the key name for this property
     */
    String key();

    /**
     * The property value.
     *
     * @return the value associated with this property
     */
    String value();
}
