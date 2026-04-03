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

import com.github.zh.engine.clz.AbstractFeature;
import com.github.zh.engine.clz.FeatureClassGenerator;
import com.github.zh.engine.clz.IFeature;
import com.github.zh.engine.co.bean.NativeFeatureBean;
import com.github.zh.engine.processor.NativeFeatureProcessor;
import javassist.CannotCompileException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.Assert.*;

/**
 * Tests for {@link FeatureClassGenerator}.
 * <p>
 * Tests the dynamic class generation using Javassist.
 * Uses a combination of unit tests and integration tests.
 * </p>
 *
 * @author zhanghuan
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FeatureClassGeneratorTest {

    @Autowired
    private NativeFeatureProcessor nativeFeatureProcessor;

    private final FeatureClassGenerator generator = new FeatureClassGenerator();

    // ==================== Class Generation Tests ====================

    /**
     * Tests that generated class can be instantiated.
     * Uses the integration test context to verify generated classes work correctly.
     */
    @Test
    public void testGenerateClassCreatesValidClass() {
        // Verify that features registered in the context have valid generated classes
        NativeFeatureBean testA = (NativeFeatureBean) nativeFeatureProcessor.getFeatureBeanMap().get("testA");

        assertNotNull("testA feature bean should exist", testA);

        IFeature feature = testA.getFeature();
        assertNotNull("Generated IFeature should not be null", feature);

        // Verify it's an instance of AbstractFeature
        assertTrue("Generated class should extend AbstractFeature",
                feature instanceof AbstractFeature);

        // Verify it implements IFeature
        assertTrue("Generated class should implement IFeature",
                feature instanceof IFeature);
    }

    /**
     * Tests that generated class can execute the original method correctly.
     */
    @Test
    public void testGeneratedClassExecutesMethod() {
        // testA returns 5 with no parameters
        NativeFeatureBean testA = (NativeFeatureBean) nativeFeatureProcessor.getFeatureBeanMap().get("testA");
        IFeature featureA = testA.getFeature();

        Object result = featureA.execute(new Object[]{});
        assertEquals("testA execution should return 5", 5, result);

        // testD returns testB + testA
        NativeFeatureBean testD = (NativeFeatureBean) nativeFeatureProcessor.getFeatureBeanMap().get("testD");
        IFeature featureD = testD.getFeature();

        Object resultD = featureD.execute(new Object[]{6, 5}); // testB=6, testA=5
        assertEquals("testD execution should return 11", 11, resultD);
    }

    /**
     * Tests that generated class handles parameters correctly.
     */
    @Test
    public void testGeneratedClassWithParameters() {
        // testB takes Integer testA and returns testA + 1
        NativeFeatureBean testB = (NativeFeatureBean) nativeFeatureProcessor.getFeatureBeanMap().get("testB");
        IFeature featureB = testB.getFeature();

        // Test with parameter value 10
        Object result = featureB.execute(new Object[]{10});
        assertEquals("testB(10) should return 11", 11, result);

        // Test with parameter value 100
        Object result2 = featureB.execute(new Object[]{100});
        assertEquals("testB(100) should return 101", 101, result2);
    }

    /**
     * Tests that generated class handles multiple parameters with different types.
     */
    @Test
    public void testGeneratedClassWithMultipleParameterTypes() {
        // testF takes Map<String, Integer> testE and Integer testC
        NativeFeatureBean testF = (NativeFeatureBean) nativeFeatureProcessor.getFeatureBeanMap().get("testF");
        IFeature featureF = testF.getFeature();

        java.util.Map<String, Integer> mapParam = new java.util.HashMap<>();
        mapParam.put("testD", 11);
        Integer intParam = 6;

        Object result = featureF.execute(new Object[]{mapParam, intParam});
        assertEquals("testF should return 1", 1, result);
    }

    // ==================== Direct Generator Tests ====================

    /**
     * Tests generateClass method directly with a simple test class.
     * Note: Due to Javassist class naming constraints, each test uses unique class names.
     */
    @Test
    public void testGenerateClassDirectly() throws Exception {
        // Create a test bean class for generation
        TestBeanForGenerator testBean = new TestBeanForGenerator();

        // Get the method info
        Method addMethod = TestBeanForGenerator.class.getMethod("add", Integer.class, Integer.class);
        Class<?>[] parameterTypes = addMethod.getParameterTypes();
        Parameter[] parameters = addMethod.getParameters();

        // Generate the class with unique name
        Class<?> generatedClass = generator.generateClass(
                parameterTypes,
                parameters,
                TestBeanForGenerator.class.getName(),
                "testBeanForGenerator",
                "generatedAddFeature_" + System.nanoTime(),
                "add"
        );

        assertNotNull("Generated class should not be null", generatedClass);

        // Verify the class can be instantiated
        Object instance = generatedClass.getDeclaredConstructors()[0].newInstance(testBean);
        assertNotNull("Generated instance should not be null", instance);

        // Verify it's an IFeature
        assertTrue("Instance should be IFeature", instance instanceof IFeature);

        // Execute and verify
        IFeature feature = (IFeature) instance;
        Object result = feature.execute(new Object[]{3, 4});
        assertEquals("add(3, 4) should return 7", 7, result);
    }

    /**
     * Tests generateClass with no parameters.
     */
    @Test
    public void testGenerateClassWithNoParameters() throws Exception {
        TestBeanForGenerator testBean = new TestBeanForGenerator();

        Method getValueMethod = TestBeanForGenerator.class.getMethod("getValue");
        Class<?>[] parameterTypes = getValueMethod.getParameterTypes();
        Parameter[] parameters = getValueMethod.getParameters();

        Class<?> generatedClass = generator.generateClass(
                parameterTypes,
                parameters,
                TestBeanForGenerator.class.getName(),
                "testBeanForGenerator",
                "generatedGetValueFeature_" + System.nanoTime(),
                "getValue"
        );

        assertNotNull("Generated class should not be null", generatedClass);

        IFeature feature = (IFeature) generatedClass.getDeclaredConstructors()[0].newInstance(testBean);
        Object result = feature.execute(new Object[]{});
        assertEquals("getValue() should return 42", 42, result);
    }

    /**
     * Tests generateClass with String parameter.
     */
    @Test
    public void testGenerateClassWithStringParameter() throws Exception {
        TestBeanForGenerator testBean = new TestBeanForGenerator();

        Method concatMethod = TestBeanForGenerator.class.getMethod("concat", String.class, String.class);
        Class<?>[] parameterTypes = concatMethod.getParameterTypes();
        Parameter[] parameters = concatMethod.getParameters();

        Class<?> generatedClass = generator.generateClass(
                parameterTypes,
                parameters,
                TestBeanForGenerator.class.getName(),
                "testBeanForGenerator",
                "generatedConcatFeature_" + System.nanoTime(),
                "concat"
        );

        IFeature feature = (IFeature) generatedClass.getDeclaredConstructors()[0].newInstance(testBean);
        Object result = feature.execute(new Object[]{"Hello", "World"});
        assertEquals("concat('Hello', 'World') should return 'HelloWorld'", "HelloWorld", result);
    }

    /**
     * Tests that parameter type and count mismatch throws exception.
     */
    @Test(expected = CannotCompileException.class)
    public void testGenerateClassWithMismatchedParameters() throws Exception {
        // Create mismatched arrays - this should throw CannotCompileException
        Class<?>[] parameterTypes = new Class<?>[]{Integer.class, Integer.class};
        Parameter[] parameters = new Parameter[]{};  // Empty parameters but 2 types

        generator.generateClass(
                parameterTypes,
                parameters,
                TestBeanForGenerator.class.getName(),
                "testBean",
                "mismatchFeature_" + System.nanoTime(),
                "add"
        );
    }

    /**
     * Tests that generated classes can handle return type correctly.
     */
    @Test
    public void testGeneratedClassReturnTypes() {
        // Integer return type
        NativeFeatureBean testA = (NativeFeatureBean) nativeFeatureProcessor.getFeatureBeanMap().get("testA");
        assertEquals("testA return type should be Integer", Integer.class, testA.getReturnType());

        // Map return type
        NativeFeatureBean testE = (NativeFeatureBean) nativeFeatureProcessor.getFeatureBeanMap().get("testE");
        assertEquals("testE return type should be Map", java.util.Map.class, testE.getReturnType());
    }

    /**
     * Helper test class for direct generator tests.
     */
    public static class TestBeanForGenerator {
        public Integer add(Integer a, Integer b) {
            return a + b;
        }

        public Integer getValue() {
            return 42;
        }

        public String concat(String a, String b) {
            return a + b;
        }
    }
}
