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

package top.volusus;

import top.volusus.engine.FeatureEngine;
import top.volusus.engine.processor.NativeFeatureProcessor;
import top.volusus.engine.properties.FeatureProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Integration tests for {@link top.volusus.engine.config.FeatureAutoConfiguration}.
 * <p>
 * Tests the auto-configuration of Feature Engine components.
 * </p>
 *
 * @author zhanghuan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FeatureAutoConfigurationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private FeatureEngine featureEngine;

    @Autowired(required = false)
    private NativeFeatureProcessor nativeFeatureProcessor;

    @Autowired(required = false)
    private FeatureProperties featureProperties;

    // ==================== Bean Creation Tests ====================

    /**
     * Tests that FeatureEngine bean is created by auto-configuration.
     */
    @Test
    public void testAutoConfigurationCreatesFeatureEngine() {
        assertNotNull("FeatureEngine bean should be created by auto-configuration", featureEngine);
        assertTrue("ApplicationContext should contain FeatureEngine bean",
                applicationContext.containsBean("featureEngine"));
    }

    /**
     * Tests that NativeFeatureProcessor bean is created by auto-configuration.
     */
    @Test
    public void testAutoConfigurationCreatesNativeFeatureProcessor() {
        assertNotNull("NativeFeatureProcessor should be created by auto-configuration", nativeFeatureProcessor);
    }

    /**
     * Tests that FeatureProperties bean is created by auto-configuration.
     */
    @Test
    public void testAutoConfigurationCreatesFeatureProperties() {
        assertNotNull("FeatureProperties bean should be created by auto-configuration", featureProperties);
    }

    // ==================== Properties Injection Tests ====================

    /**
     * Tests that configuration properties are correctly injected.
     * The test application.yml has featureThreadPoolSize: 2
     */
    @Test
    public void testFeaturePropertiesInjection() {
        assertNotNull("FeatureProperties should not be null", featureProperties);

        // application.yml sets featureThreadPoolSize to 2
        assertEquals("featureThreadPoolSize should be 2 from application.yml",
                Integer.valueOf(2), featureProperties.getFeatureThreadPoolSize());
    }

    // ==================== Default Values Tests ====================

    /**
     * Tests that all default values are correctly set.
     */
    @Test
    public void testFeaturePropertiesDefaultValues() {
        // Verify default calc timeout
        assertEquals("Default calcTimeout should be 10000",
                FeatureProperties.DEFAULT_CALC_TIMEOUT, featureProperties.getCalcTimeout());

        // Verify default thread pool name prefix
        assertEquals("Default threadPoolNamePrefix should be 'feature-pool-'",
                FeatureProperties.DEFAULT_THREAD_POOL_NAME_PREFIX, featureProperties.getThreadPoolNamePrefix());

        // Verify default thread pool max size (not overridden in application.yml)
        Integer expectedMaxSize = FeatureProperties.DEFAULT_SYSTEM_CORE_SIZE * FeatureProperties.DEFAULT_POOL_SIZE_MULTIPLIER;
        assertEquals("Default featureThreadPoolMaxSize should be cores * 2",
                expectedMaxSize, featureProperties.getFeatureThreadPoolMaxSize());
    }

    /**
     * Tests that static constants are correctly defined.
     */
    @Test
    public void testFeaturePropertiesConstants() {
        // Verify DEFAULT_CALC_TIMEOUT
        assertEquals("DEFAULT_CALC_TIMEOUT should be 10000",
                Integer.valueOf(10000), FeatureProperties.DEFAULT_CALC_TIMEOUT);

        // Verify DEFAULT_POOL_SIZE_MULTIPLIER
        assertEquals("DEFAULT_POOL_SIZE_MULTIPLIER should be 2",
                2, FeatureProperties.DEFAULT_POOL_SIZE_MULTIPLIER);

        // Verify DEFAULT_THREAD_POOL_NAME_PREFIX
        assertEquals("DEFAULT_THREAD_POOL_NAME_PREFIX should be 'feature-pool-'",
                "feature-pool-", FeatureProperties.DEFAULT_THREAD_POOL_NAME_PREFIX);

        // Verify DEFAULT_SYSTEM_CORE_SIZE is set to available processors
        assertEquals("DEFAULT_SYSTEM_CORE_SIZE should be availableProcessors",
                Integer.valueOf(Runtime.getRuntime().availableProcessors()),
                FeatureProperties.DEFAULT_SYSTEM_CORE_SIZE);
    }

    /**
     * Tests that FeatureEngine is functional after auto-configuration.
     */
    @Test
    public void testFeatureEngineIsFunctional() {
        assertNotNull("FeatureEngine should be functional", featureEngine);
        // The engine should have access to feature beans through the processor
        assertNotNull("NativeFeatureProcessor should be set up", nativeFeatureProcessor);
        assertFalse("FeatureBeanMap should not be empty",
                nativeFeatureProcessor.getFeatureBeanMap().isEmpty());
    }
}
