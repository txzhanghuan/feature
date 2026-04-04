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

/**
 * Runtime exception thrown when feature calculation fails.
 * <p>
 * This exception is used to indicate various calculation errors including:
 * <ul>
 *   <li>Calculation timeout</li>
 *   <li>No features available to calculate</li>
 *   <li>Missing required features or input parameters</li>
 *   <li>Individual feature computation failures</li>
 * </ul>
 *
 * <p>
 * As a {@link RuntimeException}, this exception does not need to be explicitly
 * declared or caught, making it suitable for use in lambda expressions and
 * functional interfaces.
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/4/3
 * Time: 10:34 AM
 * @see top.volusus.engine.FeatureEngine
 * @see top.volusus.engine.co.FeatureContext
 */
public class CalculateException extends RuntimeException {

    /**
     * Constructs a new CalculateException with the specified detail message.
     *
     * @param message the detail message describing the calculation failure
     */
    public CalculateException(String message) {
        super(message);
    }

    /**
     * Constructs a new CalculateException with the specified cause.
     *
     * @param e the cause of the calculation failure
     */
    public CalculateException(Throwable e) {
        super(e);
    }

    /**
     * Constructs a new CalculateException with the specified detail message and cause.
     *
     * @param message the detail message describing the calculation failure
     * @param cause   the cause of the calculation failure
     */
    public CalculateException(String message, Throwable cause) {
        super(message, cause);
    }

}