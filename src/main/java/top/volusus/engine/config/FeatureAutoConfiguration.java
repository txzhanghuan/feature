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

package top.volusus.engine.config;

import top.volusus.engine.FeatureEngine;
import top.volusus.engine.processor.NativeFeatureProcessor;
import top.volusus.engine.properties.FeatureProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot auto-configuration for the Feature Engine.
 * <p>
 * This configuration class automatically sets up the core components of the
 * Feature Engine when the application starts. It is enabled via Spring Boot's
 * auto-configuration mechanism through {@code META-INF/spring.factories}.
 * </p>
 * <p>
 * <b>Components Configured:</b>
 * <ul>
 *   <li>{@link NativeFeatureProcessor} - For scanning and registering feature beans</li>
 *   <li>{@link FeatureEngine} - The core computation engine</li>
 * </ul>
 * </p>
 * <p>
 * <b>Conditions:</b>
 * <ul>
 *   <li>Can be disabled by setting {@code enabled.featureEngine=false}</li>
 *   <li>Only creates beans if they are not already defined</li>
 * </ul>
 * </p>
 *
 * @author zhanghuan
 * Date: 2020/4/3
 * Time: 5:18 PM
 * @see FeatureEngine
 * @see NativeFeatureProcessor
 * @see FeatureProperties
 */
@Configuration
@ConditionalOnProperty(name = "enabled.featureEngine", matchIfMissing = true)
@ConditionalOnClass({FeatureEngine.class, NativeFeatureProcessor.class})
@EnableConfigurationProperties(FeatureProperties.class)
public class FeatureAutoConfiguration {

    /**
     * Creates the {@link NativeFeatureProcessor} bean if not already defined.
     * <p>
     * This processor scans for {@code @FeatureClass} annotated beans and registers
     * their {@code @Feature} annotated methods.
     * </p>
     *
     * @return a new NativeFeatureProcessor instance
     */
    @Bean
    @ConditionalOnMissingBean(NativeFeatureProcessor.class)
    public NativeFeatureProcessor getFeatureProcessor() {
        return new NativeFeatureProcessor();
    }

    /**
     * Creates the {@link FeatureEngine} bean if not already defined.
     * <p>
     * The engine provides the main entry point for feature calculations.
     * </p>
     *
     * @return a new FeatureEngine instance
     */
    @Bean
    @ConditionalOnMissingBean(FeatureEngine.class)
    public FeatureEngine featureEngine() {
        return new FeatureEngine();
    }
}
