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

package com.github.zh;

import com.github.zh.engine.properties.FeatureProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Integration tests for custom property values using @TestPropertySource.
 * <p>
 * Tests that custom configuration properties correctly override default values.
 * </p>
 *
 * @author zhanghuan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
        "com.github.zh.engine.feature.featureThreadPoolSize=8",
        "com.github.zh.engine.feature.featureThreadPoolMaxSize=16",
        "com.github.zh.engine.feature.calcTimeout=5000",
        "com.github.zh.engine.feature.threadPoolNamePrefix=custom-pool-"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FeatureAutoConfigurationCustomValuesTest {

    @Autowired
    private FeatureProperties featureProperties;

    /**
     * Tests that custom property values override defaults.
     */
    @Test
    public void testFeaturePropertiesCustomValues() {
        assertNotNull("FeatureProperties should not be null", featureProperties);

        // Verify custom featureThreadPoolSize
        assertEquals("featureThreadPoolSize should be 8",
                Integer.valueOf(8), featureProperties.getFeatureThreadPoolSize());

        // Verify custom featureThreadPoolMaxSize
        assertEquals("featureThreadPoolMaxSize should be 16",
                Integer.valueOf(16), featureProperties.getFeatureThreadPoolMaxSize());

        // Verify custom calcTimeout
        assertEquals("calcTimeout should be 5000",
                Integer.valueOf(5000), featureProperties.getCalcTimeout());

        // Verify custom threadPoolNamePrefix
        assertEquals("threadPoolNamePrefix should be 'custom-pool-'",
                "custom-pool-", featureProperties.getThreadPoolNamePrefix());
    }
}
