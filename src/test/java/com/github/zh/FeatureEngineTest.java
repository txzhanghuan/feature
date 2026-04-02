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

import com.github.zh.engine.FeatureEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FeatureEngineTest {

    @Autowired
    private FeatureEngine featureEngine;

    // ==================== 基本计算流程测试 ====================

    @Test
    public void testCalcWithSingleFeature() {
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testA"));
        Map<String, Object> result = featureEngine.calc(null, calcFeatures);

        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain testA", result.containsKey("testA"));
        assertEquals("testA should return 5", 5, result.get("testA"));
    }

    @Test
    public void testCalcWithDependentFeatures() {
        // testD depends on testB and testA
        // testB = testA + 1 = 6
        // testD = testB + testA = 11
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testD"));
        Map<String, Object> result = featureEngine.calc(null, calcFeatures);

        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain testD", result.containsKey("testD"));
        assertEquals("testD should return 11", 11, result.get("testD"));
        // testA is output=true by default
        assertTrue("Result should contain testA", result.containsKey("testA"));
        assertEquals("testA should return 5", 5, result.get("testA"));
        // testB is output=false, should not be in result
        assertFalse("Result should not contain testB (output=false)", result.containsKey("testB"));
    }

    // ==================== 带初始数据计算测试 ====================

    @Test
    public void testCalcWithOriginDataMap() {
        // Provide testA as origin data, it should use this value instead of calculating
        Map<String, Object> originDataMap = new HashMap<>();
        originDataMap.put("testA", 100);

        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testD"));
        Map<String, Object> result = featureEngine.calc(originDataMap, calcFeatures);

        assertNotNull("Result should not be null", result);
        // testD = testB + testA, where testB = testA + 1 = 101
        // So testD = 101 + 100 = 201
        assertTrue("Result should contain testD", result.containsKey("testD"));
        assertEquals("testD should be 201 when testA is provided as 100", 201, result.get("testD"));
    }

    @Test
    public void testCalcWithPartialOriginData() {
        // Provide testB as origin data (intermediate feature)
        Map<String, Object> originDataMap = new HashMap<>();
        originDataMap.put("testB", 50);

        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testD"));
        Map<String, Object> result = featureEngine.calc(originDataMap, calcFeatures);

        assertNotNull("Result should not be null", result);
        // testD = testB + testA = 50 + 5 = 55
        assertTrue("Result should contain testD", result.containsKey("testD"));
        assertEquals("testD should be 55 when testB is provided as 50", 55, result.get("testD"));
    }

    // ==================== 多特征并行计算测试 ====================

    @Test
    public void testCalcWithMultipleFeatures() {
        Set<String> calcFeatures = new HashSet<>(Arrays.asList("testA", "testD", "testE"));
        Map<String, Object> result = featureEngine.calc(null, calcFeatures);

        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain testA", result.containsKey("testA"));
        assertTrue("Result should contain testD", result.containsKey("testD"));
        assertTrue("Result should contain testE", result.containsKey("testE"));
        
        assertEquals("testA should return 5", 5, result.get("testA"));
        assertEquals("testD should return 11", 11, result.get("testD"));
        
        @SuppressWarnings("unchecked")
        Map<String, Integer> testEResult = (Map<String, Integer>) result.get("testE");
        assertNotNull("testE result should not be null", testEResult);
        assertEquals("testE should contain testD=11", Integer.valueOf(11), testEResult.get("testD"));
    }

    @Test
    public void testCalcAllOutputFeatures() {
        // Calculate testF which depends on all features in the chain
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testF"));
        Map<String, Object> result = featureEngine.calc(null, calcFeatures);

        assertNotNull("Result should not be null", result);
        // All output=true features should be in result
        assertTrue("Result should contain testA", result.containsKey("testA"));
        assertTrue("Result should contain testD", result.containsKey("testD"));
        assertTrue("Result should contain testE", result.containsKey("testE"));
        assertTrue("Result should contain testF", result.containsKey("testF"));
        // output=false features should not be in result
        assertFalse("Result should not contain testB", result.containsKey("testB"));
        assertFalse("Result should not contain testC", result.containsKey("testC"));
    }

    // ==================== 参数校验测试 ====================

    @Test(expected = IllegalArgumentException.class)
    public void testCalcWithNullCalcFeatures() {
        featureEngine.calc(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcWithEmptyCalcFeatures() {
        featureEngine.calc(null, new HashSet<>());
    }

    // ==================== Debug模式测试 (returnAll=true 场景) ====================

    @Test
    public void testCalcWithDebugMode() {
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testF"));
        // Use debug=true to get all intermediate results
        Map<String, Object> result = featureEngine.calc(null, calcFeatures, true);

        assertNotNull("Result should not be null", result);
        // In debug mode, ALL features should be in result, including output=false ones
        assertTrue("Debug result should contain testA", result.containsKey("testA"));
        assertTrue("Debug result should contain testB", result.containsKey("testB"));
        assertTrue("Debug result should contain testC", result.containsKey("testC"));
        assertTrue("Debug result should contain testD", result.containsKey("testD"));
        assertTrue("Debug result should contain testE", result.containsKey("testE"));
        assertTrue("Debug result should contain testF", result.containsKey("testF"));
        
        // Verify intermediate values
        assertEquals("testA should return 5", 5, result.get("testA"));
        assertEquals("testB should return 6", 6, result.get("testB"));
        assertEquals("testC should return 6", 6, result.get("testC"));
        assertEquals("testD should return 11", 11, result.get("testD"));
        assertEquals("testF should return 1", 1, result.get("testF"));
    }

    // ==================== 超时测试 ====================

    @Test
    public void testCalcWithCustomTimeout() {
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testA"));
        // Use a reasonable timeout
        Map<String, Object> result = featureEngine.calc(null, calcFeatures, 5000L);

        assertNotNull("Result should not be null", result);
        assertTrue("Result should contain testA", result.containsKey("testA"));
        assertEquals("testA should return 5", 5, result.get("testA"));
    }

    // ==================== 边界情况测试 ====================

    @Test
    public void testCalcWithNonExistentFeature() {
        // Request a feature that doesn't exist - engine should skip it
        Set<String> calcFeatures = new HashSet<>(Arrays.asList("testA", "nonExistent"));
        Map<String, Object> result = featureEngine.calc(null, calcFeatures);

        assertNotNull("Result should not be null", result);
        // testA should still be calculated
        assertTrue("Result should contain testA", result.containsKey("testA"));
        assertEquals("testA should return 5", 5, result.get("testA"));
        // nonExistent should not be in result
        assertFalse("Result should not contain nonExistent", result.containsKey("nonExistent"));
    }
}
