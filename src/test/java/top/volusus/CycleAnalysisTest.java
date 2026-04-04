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

import top.volusus.engine.co.AbstractFeatureBean;
import top.volusus.engine.co.FeatureEntity;
import top.volusus.engine.enums.FeatureEnums;
import top.volusus.engine.enums.FeatureStates;
import top.volusus.engine.tools.CycleAnalysis;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Unit tests for CycleAnalysis utility class.
 * These tests do not require Spring context.
 */
public class CycleAnalysisTest {

    // ==================== 正常 DAG（无环）测试 ====================

    @Test
    public void testNoCycleInSimpleDAG() {
        // Create a simple DAG: A -> B -> C
        Map<String, FeatureEntity> graph = new HashMap<>();
        
        graph.put("A", createFeatureEntity("A", Collections.emptyList(), Collections.singletonList("B")));
        graph.put("B", createFeatureEntity("B", Collections.singletonList("A"), Collections.singletonList("C")));
        graph.put("C", createFeatureEntity("C", Collections.singletonList("B"), Collections.emptyList()));

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertFalse("Simple DAG A->B->C should not have a cycle", hasCycle);
    }

    @Test
    public void testNoCycleInComplexDAG() {
        // Create a more complex DAG:
        //     A
        //    / \
        //   B   C
        //    \ /
        //     D
        Map<String, FeatureEntity> graph = new HashMap<>();
        
        graph.put("A", createFeatureEntity("A", Collections.emptyList(), Arrays.asList("B", "C")));
        graph.put("B", createFeatureEntity("B", Collections.singletonList("A"), Collections.singletonList("D")));
        graph.put("C", createFeatureEntity("C", Collections.singletonList("A"), Collections.singletonList("D")));
        graph.put("D", createFeatureEntity("D", Arrays.asList("B", "C"), Collections.emptyList()));

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertFalse("Diamond shaped DAG should not have a cycle", hasCycle);
    }

    @Test
    public void testNoCycleWithMultipleRoots() {
        // Create DAG with multiple roots:
        // A -> C
        // B -> C
        Map<String, FeatureEntity> graph = new HashMap<>();
        
        graph.put("A", createFeatureEntity("A", Collections.emptyList(), Collections.singletonList("C")));
        graph.put("B", createFeatureEntity("B", Collections.emptyList(), Collections.singletonList("C")));
        graph.put("C", createFeatureEntity("C", Arrays.asList("A", "B"), Collections.emptyList()));

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertFalse("DAG with multiple roots should not have a cycle", hasCycle);
    }

    // ==================== 存在循环依赖测试 ====================

    @Test
    public void testDetectSimpleCycle() {
        // Create a simple cycle: A -> B -> A
        Map<String, FeatureEntity> graph = new HashMap<>();
        
        graph.put("A", createFeatureEntity("A", Collections.singletonList("B"), Collections.singletonList("B")));
        graph.put("B", createFeatureEntity("B", Collections.singletonList("A"), Collections.singletonList("A")));

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertTrue("Simple cycle A->B->A should be detected", hasCycle);
    }

    @Test
    public void testDetectSelfLoop() {
        // Create a self-loop: A -> A
        Map<String, FeatureEntity> graph = new HashMap<>();
        
        graph.put("A", createFeatureEntity("A", Collections.singletonList("A"), Collections.singletonList("A")));

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertTrue("Self-loop A->A should be detected as cycle", hasCycle);
    }

    @Test
    public void testDetectCycleInLargerGraph() {
        // Create a graph with cycle in middle: A -> B -> C -> B
        Map<String, FeatureEntity> graph = new HashMap<>();
        
        graph.put("A", createFeatureEntity("A", Collections.emptyList(), Collections.singletonList("B")));
        graph.put("B", createFeatureEntity("B", Arrays.asList("A", "C"), Collections.singletonList("C")));
        graph.put("C", createFeatureEntity("C", Collections.singletonList("B"), Collections.singletonList("B")));

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertTrue("Cycle B->C->B should be detected", hasCycle);
    }

    @Test
    public void testAllNodesFormCycle() {
        // All nodes form a cycle: A -> B -> C -> A (no root nodes)
        Map<String, FeatureEntity> graph = new HashMap<>();
        
        graph.put("A", createFeatureEntity("A", Collections.singletonList("C"), Collections.singletonList("B")));
        graph.put("B", createFeatureEntity("B", Collections.singletonList("A"), Collections.singletonList("C")));
        graph.put("C", createFeatureEntity("C", Collections.singletonList("B"), Collections.singletonList("A")));

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertTrue("Graph where all nodes form a cycle should be detected", hasCycle);
    }

    // ==================== 空图测试 ====================

    @Test
    public void testEmptyGraph() {
        Map<String, FeatureEntity> graph = new HashMap<>();

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertFalse("Empty graph should not have a cycle", hasCycle);
    }

    // ==================== 单节点图测试 ====================

    @Test
    public void testSingleNodeWithoutSelfLoop() {
        Map<String, FeatureEntity> graph = new HashMap<>();
        
        graph.put("A", createFeatureEntity("A", Collections.emptyList(), Collections.emptyList()));

        Boolean hasCycle = CycleAnalysis.isCycle(graph);
        
        assertFalse("Single node without self-loop should not have a cycle", hasCycle);
    }

    // ==================== 辅助方法 ====================

    /**
     * Create a FeatureEntity for testing.
     */
    private FeatureEntity createFeatureEntity(String name, List<String> parents, List<String> children) {
        TestFeatureBean featureBean = new TestFeatureBean();
        featureBean.setName(name);
        featureBean.setParents(new ArrayList<>(parents));
        featureBean.setChildren(new ArrayList<>(children));
        featureBean.setOutput(true);
        
        return FeatureEntity.builder()
                .featureBean(featureBean)
                .parents(new ArrayList<>(parents))
                .children(new ArrayList<>(children))
                .featureEnum(FeatureEnums.NATIVE_FEATURE)
                .status(new AtomicReference<>(FeatureStates.INIT))
                .build();
    }

    /**
     * Simple test implementation of AbstractFeatureBean.
     */
    private static class TestFeatureBean extends AbstractFeatureBean {
        @Override
        public Object execute(Object[] args) {
            return null;
        }
    }
}
