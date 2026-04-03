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

package com.github.zh.engine.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the Feature Engine.
 * <p>
 * These properties can be configured in {@code application.yml} or {@code application.properties}
 * using the prefix {@code com.github.zh.engine.feature}.
 * </p>
 * <p>
 * <b>Example configuration:</b>
 * <pre>
 * com:
 *   github:
 *     zh:
 *       engine:
 *         feature:
 *           featureThreadPoolSize: 16
 *           featureThreadPoolMaxSize: 32
 *           calcTimeout: 5000
 *           threadPoolNamePrefix: "my-feature-pool-"
 * </pre>
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/4/2
 * Time: 2:54 PM
 * @see com.github.zh.engine.FeatureEngine
 * @see com.github.zh.engine.config.FeatureAutoConfiguration
 */
@Data
@ConfigurationProperties(prefix = "com.github.zh.engine.feature")
@Configuration
public class FeatureProperties {

    /**
     * The number of available processor cores on the system.
     */
    public static final Integer DEFAULT_SYSTEM_CORE_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * Default calculation timeout in milliseconds (10 seconds).
     */
    public static final Integer DEFAULT_CALC_TIMEOUT = 10000;

    /**
     * Default thread pool size multiplier.
     */
    public static final int DEFAULT_POOL_SIZE_MULTIPLIER = 2;

    /**
     * Default prefix for thread pool thread names.
     */
    public static final String DEFAULT_THREAD_POOL_NAME_PREFIX = "feature-pool-";

    /**
     * The core size of the feature calculation thread pool.
     * <p>
     * Defaults to system core count multiplied by {@link #DEFAULT_POOL_SIZE_MULTIPLIER}.
     * </p>
     */
    private Integer featureThreadPoolSize = DEFAULT_SYSTEM_CORE_SIZE * DEFAULT_POOL_SIZE_MULTIPLIER;

    /**
     * The maximum size of the feature calculation thread pool.
     * <p>
     * Defaults to system core count multiplied by {@link #DEFAULT_POOL_SIZE_MULTIPLIER}.
     * </p>
     */
    private Integer featureThreadPoolMaxSize = DEFAULT_SYSTEM_CORE_SIZE * DEFAULT_POOL_SIZE_MULTIPLIER;

    /**
     * The timeout in milliseconds for feature calculations.
     * <p>
     * If calculations exceed this timeout, a {@link com.github.zh.engine.exception.CalculateException}
     * is thrown.
     * </p>
     */
    private Integer calcTimeout = DEFAULT_CALC_TIMEOUT;

    /**
     * The prefix for naming threads in the calculation thread pool.
     */
    private String threadPoolNamePrefix = DEFAULT_THREAD_POOL_NAME_PREFIX;
}
