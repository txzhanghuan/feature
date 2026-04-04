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

package top.volusus.engine.tools;

import top.volusus.engine.co.FeatureEntity;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * Utility class for detecting cycles in the feature dependency graph.
 * <p>
 * This class provides methods to detect circular dependencies in a DAG
 * (Directed Acyclic Graph) of {@link FeatureEntity} instances. Cycle detection
 * is important to prevent infinite loops during feature computation.
 * </p>
 * <p>
 * <b>Algorithm:</b> Uses a BFS-based graph coloring approach:
 * <ul>
 *   <li>WHITE - Node not yet visited</li>
 *   <li>GREY - Node currently being processed (in current path)</li>
 *   <li>BLACK - Node fully processed (all children visited)</li>
 * </ul>
 * A cycle is detected when a GREY node is encountered during traversal.
 *
 * @author zhanghuan
 * @version 1.0
 * @since 2022/3/3 19:41
 * @see FeatureEntity
 * @see top.volusus.engine.co.FeatureContext
 */
@Slf4j
@UtilityClass
public class CycleAnalysis {

    /**
     * Checks whether the feature entity graph contains a cycle.
     * <p>
     * Uses BFS with three-color marking to detect back edges that would
     * indicate a cycle in the dependency graph.
     * </p>
     *
     * @param featureEntityGraph the map of feature names to their entities
     * @return true if a cycle is detected; false if the graph is acyclic
     */
    public Boolean isCycle(Map<String, FeatureEntity> featureEntityGraph) {
        List<String> roots = getRootNode(featureEntityGraph);
        if (roots.isEmpty() && !featureEntityGraph.isEmpty()) {
            return true;
        }
        LinkedList<String> queue = new LinkedList<>();
        Map<String, Color> nodeColor = new HashMap<>();
        featureEntityGraph.keySet().forEach(it -> nodeColor.put(it, Color.WHITE));

        roots.forEach(it -> {
            queue.offer(it);
            nodeColor.put(it, Color.GREY);
        });

        while (!queue.isEmpty()) {
            String current = queue.poll();
            nodeColor.put(current, Color.BLACK);

            for (String child : featureEntityGraph.get(current).getChildren()) {

                if (!featureEntityGraph.containsKey(child) || featureEntityGraph.get(child).getStatus().get().isEndStates()) {
                    continue;
                }

                if (nodeColor.get(child).equals(Color.BLACK)) {
                    log.error("Cycle detected at node: {}", child);
                    return true;
                }

                queue.offer(child);
                nodeColor.put(child, Color.GREY);
            }
        }
        return false;
    }

    /**
     * Finds all root nodes in the feature entity graph.
     * <p>
     * Root nodes are features that have no dependencies (no parents) or
     * are already in an end state.
     * </p>
     *
     * @param featureEntityGraph the map of feature names to their entities
     * @return a list of root node names
     */
    private List<String> getRootNode(Map<String, FeatureEntity> featureEntityGraph) {
        return featureEntityGraph.values().stream()
                .filter(it -> it.getParents().isEmpty() || it.getStatus().get().isEndStates())
                .map(it -> it.getFeatureBean().getName())
                .collect(Collectors.toList());
    }

    /**
     * Color states used in the cycle detection algorithm.
     */
    enum Color {
        /** Not yet visited */
        WHITE,
        /** Currently being processed (in current traversal path) */
        GREY,
        /** Fully processed (all children visited) */
        BLACK
    }
}
