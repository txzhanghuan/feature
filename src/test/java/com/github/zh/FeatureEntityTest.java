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

import com.github.zh.engine.co.AbstractFeatureBean;
import com.github.zh.engine.co.FeatureContext;
import com.github.zh.engine.co.FeatureEntity;
import com.github.zh.engine.enums.FeatureEnums;
import com.github.zh.engine.enums.FeatureStates;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Unit tests for FeatureEntity.
 * These tests do not require Spring context.
 */
public class FeatureEntityTest {

    private ThreadPoolExecutor pool;
    private FeatureContext featureContext;

    @Before
    public void setUp() {
        pool = new ThreadPoolExecutor(
                2, 4, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100)
        );
        featureContext = new FeatureContext();
        setFieldValue(featureContext, "pool", pool);
    }

    // ==================== 初始状态测试 ====================

    @Test
    public void testInitialState() {
        FeatureEntity entity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(createMockFeatureBean("testFeature", true))
                .build();

        assertEquals("Initial state should be INIT", FeatureStates.INIT, entity.getStatus().get());
        assertNull("Result should be null initially", entity.getResult());
        assertNull("Error should be null initially", entity.getError());
        assertNull("ErrorParent should be null initially", entity.getErrorParent());
    }

    // ==================== checkParamsReady 测试 ====================

    @Test
    public void testCheckParamsReadyAllReady() throws Exception {
        // Create parent entity with SUCCESS status
        FeatureEntity parentEntity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>(Collections.singletonList("child")))
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .status(new AtomicReference<>(FeatureStates.SUCCESS))
                .result(10)
                .featureBean(createMockFeatureBean("parent", true))
                .build();

        featureContext.getFeatureEntitiesPool().put("parent", parentEntity);

        FeatureEntity childEntity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>(Collections.singletonList("parent")))
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(createMockFeatureBean("child", true))
                .build();

        // Use reflection to call private method
        Method checkParamsReady = FeatureEntity.class.getDeclaredMethod("checkParamsReady");
        checkParamsReady.setAccessible(true);
        boolean result = (boolean) checkParamsReady.invoke(childEntity);

        assertTrue("Should return true when all parents are ready", result);
    }

    @Test
    public void testCheckParamsReadyNotReady() throws Exception {
        // Create parent entity with INIT status (not ready)
        FeatureEntity parentEntity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>(Collections.singletonList("child")))
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(createMockFeatureBean("parent", true))
                .build();

        featureContext.getFeatureEntitiesPool().put("parent", parentEntity);

        FeatureEntity childEntity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>(Collections.singletonList("parent")))
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(createMockFeatureBean("child", true))
                .build();

        // Use reflection to call private method
        Method checkParamsReady = FeatureEntity.class.getDeclaredMethod("checkParamsReady");
        checkParamsReady.setAccessible(true);
        boolean result = (boolean) checkParamsReady.invoke(childEntity);

        assertFalse("Should return false when parent is not ready", result);
    }

    // ==================== 执行成功测试 ====================

    @Test
    public void testExecuteSuccess() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        setFieldValue(featureContext, "countDownLatch", latch);

        AbstractFeatureBean mockBean = createMockFeatureBean("testFeature", true);

        FeatureEntity entity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(mockBean)
                .build();

        featureContext.getFeatureEntitiesPool().put("testFeature", entity);

        entity.execute(null);

        assertEquals("Status should be SUCCESS after successful execution",
                FeatureStates.SUCCESS, entity.getStatus().get());
        assertEquals("Result should be set correctly", 42, entity.getResult());
        assertNull("Error should remain null on success", entity.getError());
        assertEquals("CountDownLatch should be decremented", 0, latch.getCount());
    }

    // ==================== 执行失败测试 ====================

    @Test
    public void testExecuteFailure() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        setFieldValue(featureContext, "countDownLatch", latch);

        AbstractFeatureBean failingBean = new AbstractFeatureBean() {
            {
                this.name = "failingFeature";
                this.output = true;
                this.parents = new ArrayList<>();
                this.children = new ArrayList<>();
            }

            @Override
            public Object execute(Object[] args) {
                throw new RuntimeException("Simulated failure");
            }
        };

        FeatureEntity entity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(failingBean)
                .build();

        featureContext.getFeatureEntitiesPool().put("failingFeature", entity);

        entity.execute(null);

        assertEquals("Status should be FAILED after execution failure",
                FeatureStates.FAILED, entity.getStatus().get());
        assertNotNull("Error should be set on failure", entity.getError());
        assertTrue("FastFail should be set on context", featureContext.isFastFail());
        assertEquals("CountDownLatch should be decremented", 0, latch.getCount());
    }

    // ==================== 快速失败传播测试 ====================

    @Test
    public void testFastFailPropagation() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        setFieldValue(featureContext, "countDownLatch", latch);
        featureContext.setFastFail(true);

        FeatureEntity entity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(createMockFeatureBean("testFeature", true))
                .build();

        featureContext.getFeatureEntitiesPool().put("testFeature", entity);

        entity.execute(null);

        assertEquals("Status should be FAILED due to fast fail",
                FeatureStates.FAILED, entity.getStatus().get());
        assertEquals("CountDownLatch should be decremented", 0, latch.getCount());
    }

    // ==================== 通知子节点测试 ====================

    @Test
    public void testNotifyChildren() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        setFieldValue(featureContext, "countDownLatch", latch);

        // Create parent entity
        FeatureEntity parentEntity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>(Collections.singletonList("child")))
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(createMockFeatureBean("parent", true))
                .build();

        // Create child entity
        AbstractFeatureBean childBean = new AbstractFeatureBean() {
            {
                this.name = "child";
                this.output = true;
                this.parents = new ArrayList<>(Collections.singletonList("parent"));
                this.children = new ArrayList<>();
            }

            @Override
            public Object execute(Object[] args) {
                return (Integer) args[0] + 10;
            }
        };

        FeatureEntity childEntity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>(Collections.singletonList("parent")))
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(childBean)
                .build();

        featureContext.getFeatureEntitiesPool().put("parent", parentEntity);
        featureContext.getFeatureEntitiesPool().put("child", childEntity);

        // Execute parent, which should notify child
        parentEntity.execute(null);

        // Wait for child to complete
        boolean completed = latch.await(5, TimeUnit.SECONDS);

        assertTrue("Both entities should complete", completed);
        assertEquals("Parent should be SUCCESS", FeatureStates.SUCCESS, parentEntity.getStatus().get());
        assertEquals("Child should be SUCCESS", FeatureStates.SUCCESS, childEntity.getStatus().get());
        assertEquals("Child result should be parent result + 10", 52, childEntity.getResult());
    }

    // ==================== 状态转换测试 ====================

    @Test
    public void testStateTransitions() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        setFieldValue(featureContext, "countDownLatch", latch);

        // Create a slow executing bean to observe state transitions
        final AtomicReference<FeatureStates> observedState = new AtomicReference<>();

        AbstractFeatureBean slowBean = new AbstractFeatureBean() {
            {
                this.name = "slowFeature";
                this.output = true;
                this.parents = new ArrayList<>();
                this.children = new ArrayList<>();
            }

            @Override
            public Object execute(Object[] args) {
                // Record the state during execution
                FeatureEntity entity = featureContext.getFeatureEntitiesPool().get("slowFeature");
                observedState.set(entity.getStatus().get());
                return 100;
            }
        };

        FeatureEntity entity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(slowBean)
                .build();

        featureContext.getFeatureEntitiesPool().put("slowFeature", entity);

        // Verify initial state
        assertEquals("Initial state should be INIT", FeatureStates.INIT, entity.getStatus().get());

        entity.execute(null);

        // During execution, state should have been PROCESSING
        assertEquals("State during execution should be PROCESSING",
                FeatureStates.PROCESSING, observedState.get());

        // After execution, state should be SUCCESS
        assertEquals("Final state should be SUCCESS", FeatureStates.SUCCESS, entity.getStatus().get());
    }

    @Test
    public void testStateTransitionsOnFailure() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        setFieldValue(featureContext, "countDownLatch", latch);

        final AtomicReference<FeatureStates> observedState = new AtomicReference<>();

        AbstractFeatureBean failingBean = new AbstractFeatureBean() {
            {
                this.name = "failingFeature";
                this.output = true;
                this.parents = new ArrayList<>();
                this.children = new ArrayList<>();
            }

            @Override
            public Object execute(Object[] args) {
                FeatureEntity entity = featureContext.getFeatureEntitiesPool().get("failingFeature");
                observedState.set(entity.getStatus().get());
                throw new RuntimeException("Intentional failure");
            }
        };

        FeatureEntity entity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(failingBean)
                .build();

        featureContext.getFeatureEntitiesPool().put("failingFeature", entity);

        assertEquals("Initial state should be INIT", FeatureStates.INIT, entity.getStatus().get());

        entity.execute(null);

        assertEquals("State during execution should be PROCESSING",
                FeatureStates.PROCESSING, observedState.get());
        assertEquals("Final state should be FAILED", FeatureStates.FAILED, entity.getStatus().get());
    }

    // ==================== 父节点失败传播测试 ====================

    @Test
    public void testParentFailurePropagation() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        setFieldValue(featureContext, "countDownLatch", latch);

        // Create failed parent entity
        FeatureEntity parentEntity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>())
                .children(new ArrayList<>(Collections.singletonList("child")))
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .status(new AtomicReference<>(FeatureStates.FAILED))
                .error(new RuntimeException("Parent failed"))
                .featureBean(createMockFeatureBean("parent", true))
                .build();

        featureContext.getFeatureEntitiesPool().put("parent", parentEntity);

        FeatureEntity childEntity = FeatureEntity.builder()
                .featureContext(featureContext)
                .parents(new ArrayList<>(Collections.singletonList("parent")))
                .children(new ArrayList<>())
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .featureBean(createMockFeatureBean("child", true))
                .build();

        featureContext.getFeatureEntitiesPool().put("child", childEntity);

        childEntity.execute(null);

        assertEquals("Child should fail due to parent failure",
                FeatureStates.FAILED, childEntity.getStatus().get());
        assertEquals("ErrorParent should be set to parent", "parent", childEntity.getErrorParent());
        assertTrue("FastFail should be set on context", featureContext.isFastFail());
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
}
