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

package com.github.zh.engine.annotation.properties;

import java.lang.annotation.*;

/**
 * Container annotation for repeatable {@link Property} annotations.
 * <p>
 * This annotation is automatically used as the container when multiple
 * {@link Property} annotations are applied to the same feature method.
 * In most cases, you should use {@link Property} directly with the
 * {@code @Repeatable} mechanism rather than using this annotation explicitly.
 * </p>
 * <p>
 * <b>Example:</b>
 * <pre>
 * {@code
 * @Feature
 * @Property(key = "timeout", value = "1000")
 * @Property(key = "retry", value = "3")
 * public Object myFeature(Object input) {
 *     // ...
 * }
 * }
 * </pre>
 * </p>
 *
 * @author zhanghuan
 * @version 1.0
 * @date 2021/9/17 16:41
 * @see Property
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Properties {

    /**
     * Returns the array of {@link Property} annotations.
     *
     * @return an array of Property annotations
     */
    Property[] value();
}
