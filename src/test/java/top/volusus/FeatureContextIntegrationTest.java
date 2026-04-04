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
import top.volusus.engine.co.AbstractFeatureBean;
import top.volusus.engine.co.FeatureContext;
import top.volusus.engine.co.FeatureDAGBuilder;
import top.volusus.engine.co.FeatureEntity;
import top.volusus.engine.enums.FeatureEnums;
import top.volusus.engine.enums.FeatureStates;
import top.volusus.engine.exception.CalculateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Integration tests for FeatureContext.
 * These tests require Spring context.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FeatureContextIntegrationTest {

    @Autowired
    private FeatureEngine featureEngine;

    private ThreadPoolExecutor createTestPool() {
        return new ThreadPoolExecutor(
                2, 4, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100)
        );
    }

    // ==================== checkFail 测试 ====================

    @Test
    public void testCheckFailWithNoErrors() {
        // Calculate a simple feature that should succeed
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testA"));
        Map<String, Object> result = featureEngine.calc(null, calcFeatures);

        // If checkFail throws, this assertion would not be reached
        assertNotNull("Result should not be null when no errors", result);
        assertTrue("Result should contain testA", result.containsKey("testA"));
    }

    @Test
    public void testCheckFailWithErrors() {
        FeatureContext context = new FeatureContext();
        ThreadPoolExecutor pool = createTestPool();

        try {
            // Set up the context with a failed feature
            setFieldValue(context, "pool", pool);
            context.setFastFail(true);

            // Create a failed feature entity with an error and no errorParent (root error)
            AbstractFeatureBean failedBean = new AbstractFeatureBean() {
                {
                    this.name = "failedFeature";
                    this.output = true;
                    this.parents = new ArrayList<>();
                    this.children = new ArrayList<>();
                }
                @Override
                public Object execute(Object[] args) {
                    return 42;
                }
            };
            
            FeatureEntity failedEntity = FeatureEntity.builder()
                    .featureContext(context)
                    .parents(new ArrayList<>())
                    .children(new ArrayList<>())
                    .featureEnum(FeatureEnums.NATIVE_FEATURE)
                    .status(new AtomicReference<>(FeatureStates.FAILED))
                    .error(new RuntimeException("Test error"))
                    .errorParent(null) // This is the root error
                    .featureBean(failedBean)
                    .build();

            context.getFeatureEntitiesPool().put("failedFeature", failedEntity);

            // Calling getCalcResult should trigger checkFail and throw exception
            try {
                context.getCalcResult(false);
                fail("Should throw CalculateException when there are errors");
            } catch (CalculateException e) {
                assertTrue("Exception message should contain feature name",
                        e.getMessage().contains("failedFeature"));
                assertNotNull("Exception should have a cause", e.getCause());
            }
        } finally {
            pool.shutdown();
        }
    }

    // ==================== getRootErrorFeature 测试 ====================

    @Test
    public void testGetRootErrorFeature() {
        FeatureContext context = new FeatureContext();
        ThreadPoolExecutor pool = createTestPool();

        try {
            setFieldValue(context, "pool", pool);

            // Create a chain: rootFeature (failed) -> middleFeature (failed) -> leafFeature (failed)
            // The rootFeature has no errorParent, so getRootErrorFeature should return its name
            AbstractFeatureBean rootBean = new AbstractFeatureBean() {
                {
                    this.name = "rootFeature";
                    this.output = true;
                    this.parents = new ArrayList<>();
                    this.children = new ArrayList<>(Collections.singletonList("middleFeature"));
                }
                @Override
                public Object execute(Object[] args) {
                    return 42;
                }
            };
            
            FeatureEntity rootEntity = FeatureEntity.builder()
                    .featureContext(context)
                    .parents(new ArrayList<>())
                    .children(new ArrayList<>(Collections.singletonList("middleFeature")))
                    .featureEnum(FeatureEnums.NATIVE_FEATURE)
                    .status(new AtomicReference<>(FeatureStates.FAILED))
                    .error(new RuntimeException("Root cause"))
                    .errorParent(null) // This is the root
                    .featureBean(rootBean)
                    .build();

            AbstractFeatureBean middleBean = new AbstractFeatureBean() {
                {
                    this.name = "middleFeature";
                    this.output = true;
                    this.parents = new ArrayList<>(Collections.singletonList("rootFeature"));
                    this.children = new ArrayList<>(Collections.singletonList("leafFeature"));
                }
                @Override
                public Object execute(Object[] args) {
                    return 42;
                }
            };
            
            FeatureEntity middleEntity = FeatureEntity.builder()
                    .featureContext(context)
                    .parents(new ArrayList<>(Collections.singletonList("rootFeature")))
                    .children(new ArrayList<>(Collections.singletonList("leafFeature")))
                    .featureEnum(FeatureEnums.NATIVE_FEATURE)
                    .status(new AtomicReference<>(FeatureStates.FAILED))
                    .error(new RuntimeException("Middle error"))
                    .errorParent("rootFeature")
                    .featureBean(middleBean)
                    .build();

            AbstractFeatureBean leafBean = new AbstractFeatureBean() {
                {
                    this.name = "leafFeature";
                    this.output = true;
                    this.parents = new ArrayList<>(Collections.singletonList("middleFeature"));
                    this.children = new ArrayList<>();
                }
                @Override
                public Object execute(Object[] args) {
                    return 42;
                }
            };
            
            FeatureEntity leafEntity = FeatureEntity.builder()
                    .featureContext(context)
                    .parents(new ArrayList<>(Collections.singletonList("middleFeature")))
                    .children(new ArrayList<>())
                    .featureEnum(FeatureEnums.NATIVE_FEATURE)
                    .status(new AtomicReference<>(FeatureStates.FAILED))
                    .error(new RuntimeException("Leaf error"))
                    .errorParent("middleFeature")
                    .featureBean(leafBean)
                    .build();

            context.getFeatureEntitiesPool().put("rootFeature", rootEntity);
            context.getFeatureEntitiesPool().put("middleFeature", middleEntity);
            context.getFeatureEntitiesPool().put("leafFeature", leafEntity);

            // Test tracing back from leaf to root
            String rootError = context.getRootErrorFeature(leafEntity);
            assertEquals("Should trace back to root feature", "rootFeature", rootError);

            // Test from middle
            String rootErrorFromMiddle = context.getRootErrorFeature(middleEntity);
            assertEquals("Should trace back to root from middle", "rootFeature", rootErrorFromMiddle);

            // Test from root itself
            String rootErrorFromRoot = context.getRootErrorFeature(rootEntity);
            assertEquals("Root should return itself", "rootFeature", rootErrorFromRoot);
        } finally {
            pool.shutdown();
        }
    }

    // ==================== getCalcResult 过滤测试 ====================

    @Test
    public void testGetCalcResultFiltersOriginData() {
        // testD depends on testA and testB
        // Provide testA as origin data, calculate testD
        Map<String, Object> originDataMap = new HashMap<>();
        originDataMap.put("testA", 100);

        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testD"));
        Map<String, Object> result = featureEngine.calc(originDataMap, calcFeatures);

        // In non-debug mode, origin data should not be in result
        assertFalse("Origin data (testA) should not be in result when provided as input",
                result.containsKey("testA") && result.get("testA").equals(100));
        assertTrue("Calculated feature testD should be in result", result.containsKey("testD"));
    }

    @Test
    public void testGetCalcResultFiltersNonOutputFeatures() {
        // Calculate testD which depends on testB (output=false) and testA
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testD"));
        Map<String, Object> result = featureEngine.calc(null, calcFeatures);

        // testB is marked as output=false, should not be in result
        assertFalse("testB (output=false) should not be in result", result.containsKey("testB"));
        assertTrue("testA (output=true) should be in result", result.containsKey("testA"));
        assertTrue("testD (output=true) should be in result", result.containsKey("testD"));
    }

    // ==================== Debug 模式测试 ====================

    @Test
    public void testGetCalcResultDebugMode() {
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testD"));
        // Use debug=true to get all results including intermediate ones
        Map<String, Object> result = featureEngine.calc(null, calcFeatures, true);

        // In debug mode, all features should be returned
        assertTrue("Debug mode should include testA", result.containsKey("testA"));
        assertTrue("Debug mode should include testB", result.containsKey("testB"));
        assertTrue("Debug mode should include testD", result.containsKey("testD"));

        // Verify intermediate values
        assertEquals("testA should be 5", 5, result.get("testA"));
        assertEquals("testB should be 6", 6, result.get("testB"));
        assertEquals("testD should be 11", 11, result.get("testD"));
    }

    @Test
    public void testGetCalcResultDebugModeWithOriginData() {
        Map<String, Object> originDataMap = new HashMap<>();
        originDataMap.put("inputParam", 999);

        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testA"));
        Map<String, Object> result = featureEngine.calc(originDataMap, calcFeatures, true);

        // In debug mode, origin data should also be included
        assertTrue("Debug mode should include inputParam", result.containsKey("inputParam"));
        assertEquals("inputParam should be 999", 999, result.get("inputParam"));
        assertTrue("Debug mode should include testA", result.containsKey("testA"));
    }

    // ==================== 循环检测测试 ====================

    @Test
    public void testCycleDetectionEnabled() {
        // Note: The cycleAnalysis method is currently disabled (commented out)
        // This test verifies the current behavior
        // When cycle detection is re-enabled, this test should verify that
        // cyclic dependencies are properly detected and reported

        // For now, we just verify that calculation works with non-cyclic dependencies
        Set<String> calcFeatures = new HashSet<>(Collections.singletonList("testF"));
        Map<String, Object> result = featureEngine.calc(null, calcFeatures);

        assertNotNull("Result should not be null for non-cyclic dependencies", result);
        assertTrue("testF should be calculated", result.containsKey("testF"));
    }

    // ==================== executeAll 测试 ====================

    @Test(expected = CalculateException.class)
    public void testExecuteAllWithNoFeatures() throws InterruptedException {
        FeatureContext context = new FeatureContext();
        ThreadPoolExecutor pool = createTestPool();

        try {
            setFieldValue(context, "pool", pool);
            setFieldValue(context, "countDownLatch", new CountDownLatch(0));

            // Should throw CalculateException("无变量需计算")
            context.executeAll(1000L, null);
        } finally {
            pool.shutdown();
        }
    }

    @Test
    public void testExecuteAllWithTimeout() throws InterruptedException {
        FeatureContext context = new FeatureContext();
        ThreadPoolExecutor pool = createTestPool();

        try {
            setFieldValue(context, "pool", pool);
            setFieldValue(context, "needCalcFeaturesCount", 1);
            setFieldValue(context, "countDownLatch", new CountDownLatch(1));

            // Create a slow feature that will cause timeout
            AbstractFeatureBean slowBean = new AbstractFeatureBean() {
                {
                    this.name = "slowFeature";
                    this.output = true;
                    this.parents = new ArrayList<>();
                    this.children = new ArrayList<>();
                }

                @Override
                public Object execute(Object[] args) {
                    try {
                        Thread.sleep(5000); // Sleep for 5 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return 1;
                }
            };

            FeatureEntity slowEntity = FeatureEntity.builder()
                    .featureContext(context)
                    .parents(new ArrayList<>())
                    .children(new ArrayList<>())
                    .featureEnum(FeatureEnums.NATIVE_FEATURE)
                    .featureBean(slowBean)
                    .build();

            context.getFeatureEntitiesPool().put("slowFeature", slowEntity);

            try {
                context.executeAll(100L, null); // Very short timeout
                fail("Should throw CalculateException due to timeout");
            } catch (CalculateException e) {
                assertTrue("Exception message should indicate timeout",
                        e.getMessage().contains("timeout") || e.getMessage().contains("Timeout"));
            }
        } finally {
            pool.shutdown();
        }
    }

    // ==================== 初始化测试 ====================

    @Test
    public void testInitWithOriginData() {
        FeatureContext context = new FeatureContext();
        ThreadPoolExecutor pool = createTestPool();

        try {
            Map<String, Object> originDataMap = new HashMap<>();
            originDataMap.put("input1", 100);
            originDataMap.put("input2", "test");

            // Use reflection to access init method behavior
            setFieldValue(context, "pool", pool);

            // Use FeatureDAGBuilder to initialize origin data
            FeatureDAGBuilder dagBuilder = new FeatureDAGBuilder(
                    context,
                    context.getFeatureEntitiesPool(),
                    new HashMap<>(),
                    originDataMap,
                    new HashSet<>(),
                    null
            );
            dagBuilder.build();

            // Verify origin data was added to the pool
            assertTrue("Should contain input1", context.getFeatureEntitiesPool().containsKey("input1"));
            assertTrue("Should contain input2", context.getFeatureEntitiesPool().containsKey("input2"));

            // Verify origin data entities have SUCCESS status
            assertEquals("input1 status should be SUCCESS",
                    FeatureStates.SUCCESS, context.getFeatureEntitiesPool().get("input1").getStatus().get());
            assertEquals("input2 status should be SUCCESS",
                    FeatureStates.SUCCESS, context.getFeatureEntitiesPool().get("input2").getStatus().get());

            // Verify origin data results are set
            assertEquals("input1 result should be 100", 100, context.getFeatureEntitiesPool().get("input1").getResult());
            assertEquals("input2 result should be test", "test", context.getFeatureEntitiesPool().get("input2").getResult());

            // Verify origin data is marked as ORIGIN_DATA type
            assertEquals("input1 should be ORIGIN_DATA type",
                    FeatureEnums.ORIGIN_DATA, context.getFeatureEntitiesPool().get("input1").getFeatureEnum());
        } finally {
            pool.shutdown();
        }
    }

    // ==================== 辅助方法 ====================

    private AbstractFeatureBean createMockFeatureBean(String name, boolean output) {
        return new AbstractFeatureBean() {
            {
                this.name = name;
                this.output = output;
                this.parents = new ArrayList<>();
                this.children = new ArrayList<>();
            }

            @Override
            public Object execute(Object[] args) {
                return 42;
            }
        };
    }

    private void setFieldValue(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private Object invokePrivateMethod(Object target, String methodName,
                                        Class<?>[] paramTypes, Object... args) {
        try {
            java.lang.reflect.Method method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }
}
