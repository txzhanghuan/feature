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

package top.volusus.engine.exception;

import org.springframework.beans.BeansException;

/**
 * Exception thrown when feature bean creation fails during application startup.
 * <p>
 * This exception extends {@link BeansException} to integrate with Spring's
 * bean creation error handling. It is thrown by
 * {@link top.volusus.engine.processor.NativeFeatureProcessor} when:
 * <ul>
 *   <li>Dynamic class generation fails (Javassist errors)</li>
 *   <li>Feature method signature is invalid</li>
 *   <li>Bean instantiation fails</li>
 * </ul>
 *
 * @author zhanghuan
 * Date: 2020/4/14
 * Time: 2:49 PM
 * @see top.volusus.engine.processor.NativeFeatureProcessor
 * @see BeansException
 */
public class FeatureCreationException extends BeansException {

    /**
     * Constructs a new FeatureCreationException with the specified detail message.
     *
     * @param msg the detail message describing the creation failure
     */
    public FeatureCreationException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new FeatureCreationException with the specified detail message and cause.
     *
     * @param msg   the detail message describing the creation failure
     * @param cause the underlying cause of the failure
     */
    public FeatureCreationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
