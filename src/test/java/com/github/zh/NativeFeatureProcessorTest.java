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
import com.github.zh.engine.co.bean.NativeFeatureBean;
import com.github.zh.engine.processor.NativeFeatureProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Integration tests for {@link NativeFeatureProcessor}.
 * <p>
 * Tests the annotation processor's ability to scan, register, and configure feature beans.
 * </p>
 *
 * @author zhanghuan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NativeFeatureProcessorTest {

    @Autowired
    private NativeFeatureProcessor nativeFeatureProcessor;

    // ==================== Feature Bean Registration Tests ====================

    /**
     * Tests that @Feature annotated methods are correctly registered as feature beans.
     */
    @Test
    public void testFeatureBeansRegistered() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        assertNotNull("FeatureBeanMap should not be null", featureBeanMap);
        assertFalse("FeatureBeanMap should not be empty", featureBeanMap.isEmpty());

        // Verify all expected features from Test.java are registered
        assertTrue("testA should be registered", featureBeanMap.containsKey("testA"));
        assertTrue("testB should be registered", featureBeanMap.containsKey("testB"));
        assertTrue("testC should be registered", featureBeanMap.containsKey("testC"));
        assertTrue("testD should be registered", featureBeanMap.containsKey("testD"));
        assertTrue("testE should be registered", featureBeanMap.containsKey("testE"));
        assertTrue("testF should be registered", featureBeanMap.containsKey("testF"));
    }

    /**
     * Tests that feature bean output flag is correctly set from annotation.
     */
    @Test
    public void testFeatureBeanOutputFlag() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        // testA has default output=true
        AbstractFeatureBean testA = featureBeanMap.get("testA");
        assertTrue("testA should have output=true", testA.isOutput());

        // testB has output=false
        AbstractFeatureBean testB = featureBeanMap.get("testB");
        assertFalse("testB should have output=false", testB.isOutput());

        // testC has output=false
        AbstractFeatureBean testC = featureBeanMap.get("testC");
        assertFalse("testC should have output=false", testC.isOutput());

        // testD has default output=true
        AbstractFeatureBean testD = featureBeanMap.get("testD");
        assertTrue("testD should have output=true", testD.isOutput());
    }

    // ==================== Dependency Relationship Tests ====================

    /**
     * Tests that parent-child dependencies between features are correctly established.
     */
    @Test
    public void testFeatureBeanDependencies() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        // testA has no parents
        AbstractFeatureBean testA = featureBeanMap.get("testA");
        assertTrue("testA should have no parents", testA.getParents().isEmpty());

        // testB depends on testA
        AbstractFeatureBean testB = featureBeanMap.get("testB");
        List<String> testBParents = testB.getParents();
        assertEquals("testB should have 1 parent", 1, testBParents.size());
        assertTrue("testB should depend on testA", testBParents.contains("testA"));

        // testD depends on testB and testA
        AbstractFeatureBean testD = featureBeanMap.get("testD");
        List<String> testDParents = testD.getParents();
        assertEquals("testD should have 2 parents", 2, testDParents.size());
        assertTrue("testD should depend on testB", testDParents.contains("testB"));
        assertTrue("testD should depend on testA", testDParents.contains("testA"));
    }

    /**
     * Tests that children relationships are correctly populated.
     */
    @Test
    public void testFeatureBeanChildren() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        // testA should have children: testB, testC, testD
        AbstractFeatureBean testA = featureBeanMap.get("testA");
        List<String> testAChildren = testA.getChildren();
        assertTrue("testA should have testB as child", testAChildren.contains("testB"));
        assertTrue("testA should have testC as child", testAChildren.contains("testC"));
        assertTrue("testA should have testD as child", testAChildren.contains("testD"));

        // testB should have testD as child
        AbstractFeatureBean testB = featureBeanMap.get("testB");
        List<String> testBChildren = testB.getChildren();
        assertTrue("testB should have testD as child", testBChildren.contains("testD"));

        // testD should have testE as child
        AbstractFeatureBean testD = featureBeanMap.get("testD");
        List<String> testDChildren = testD.getChildren();
        assertTrue("testD should have testE as child", testDChildren.contains("testE"));
    }

    // ==================== FeatureBeanPostProcessor Tests ====================

    /**
     * Tests that FeatureBeanPostProcessor (like SignUpFeatureBeanPostProcessor) is correctly invoked.
     * The SignUpFeatureBeanPostProcessor prints feature name and properties, verifying it was called.
     */
    @Test
    public void testFeatureBeanPostProcessorInvoked() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        // If the post processor was invoked, the feature beans should still be valid
        // The SignUpFeatureBeanPostProcessor just prints and returns the bean unchanged
        for (AbstractFeatureBean bean : featureBeanMap.values()) {
            assertNotNull("Feature bean should not be null after post-processing", bean);
            assertNotNull("Feature bean name should not be null", bean.getName());
        }

        // Verify feature beans are NativeFeatureBean instances (target of SignUpFeatureBeanPostProcessor)
        AbstractFeatureBean testA = featureBeanMap.get("testA");
        assertTrue("testA should be NativeFeatureBean", testA instanceof NativeFeatureBean);
    }

    // ==================== @Properties/@Property Annotation Tests ====================

    /**
     * Tests that @Properties/@Property annotations are correctly parsed.
     */
    @Test
    public void testFeatureBeanProperties() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        // testB has @Property annotations
        NativeFeatureBean testB = (NativeFeatureBean) featureBeanMap.get("testB");
        Map<String, String> properties = testB.getProperties();

        assertNotNull("testB properties should not be null", properties);
        assertFalse("testB properties should not be empty", properties.isEmpty());
        assertEquals("testB should have property a=b", "b", properties.get("a"));
        assertEquals("testB should have property b=b", "b", properties.get("b"));
    }

    /**
     * Tests that features without @Property annotations have empty properties map.
     */
    @Test
    public void testFeatureBeanWithNoProperties() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        // testA has no @Property annotations
        NativeFeatureBean testA = (NativeFeatureBean) featureBeanMap.get("testA");
        Map<String, String> properties = testA.getProperties();

        assertNotNull("testA properties should not be null", properties);
        assertTrue("testA properties should be empty", properties.isEmpty());
    }

    // ==================== Multiple Dependencies Tests ====================

    /**
     * Tests that features with multiple dependencies are correctly processed.
     */
    @Test
    public void testFeatureBeanWithMultipleDependencies() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        // testF depends on testE (Map type) and testC (Integer type)
        NativeFeatureBean testF = (NativeFeatureBean) featureBeanMap.get("testF");

        List<String> parents = testF.getParents();
        assertEquals("testF should have 2 parents", 2, parents.size());
        assertTrue("testF should depend on testE", parents.contains("testE"));
        assertTrue("testF should depend on testC", parents.contains("testC"));

        // Verify return type is set
        assertNotNull("testF return type should not be null", testF.getReturnType());
        assertEquals("testF return type should be Integer", Integer.class, testF.getReturnType());
    }

    /**
     * Tests that feature metadata is correctly stored.
     */
    @Test
    public void testFeatureBeanMetadata() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        NativeFeatureBean testA = (NativeFeatureBean) featureBeanMap.get("testA");

        // Verify feature metadata
        assertNotNull("testA featureMetaData should not be null", testA.getFeatureMetaData());
        assertNotNull("testA featureClass should not be null", testA.getFeatureClass());

        // Verify IFeature instance is created
        assertNotNull("testA feature (IFeature) should not be null", testA.getFeature());
    }

    /**
     * Tests that feature beans can be executed.
     */
    @Test
    public void testFeatureBeanExecution() {
        ConcurrentHashMap<String, AbstractFeatureBean> featureBeanMap = nativeFeatureProcessor.getFeatureBeanMap();

        // testA takes no arguments and returns 5
        NativeFeatureBean testA = (NativeFeatureBean) featureBeanMap.get("testA");
        Object result = testA.execute(new Object[]{});
        assertEquals("testA should return 5", 5, result);

        // testB takes testA as argument and returns testA + 1
        NativeFeatureBean testB = (NativeFeatureBean) featureBeanMap.get("testB");
        Object resultB = testB.execute(new Object[]{5});
        assertEquals("testB should return 6 when testA=5", 6, resultB);

        // testD takes testB and testA as arguments
        NativeFeatureBean testD = (NativeFeatureBean) featureBeanMap.get("testD");
        Object resultD = testD.execute(new Object[]{6, 5});
        assertEquals("testD should return 11 when testB=6, testA=5", 11, resultD);
    }
}
