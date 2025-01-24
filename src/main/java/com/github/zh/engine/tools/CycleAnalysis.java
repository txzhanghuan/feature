package com.github.zh.engine.tools;

import com.github.zh.engine.co.FeatureEntity;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

/**
 * @author ahuan.zh
 * @version 1.0
 * @date 2022/3/3 19:41
 */
@Slf4j
@UtilityClass
public class CycleAnalysis {

    /**
     * 环分析
     *
     * @return true:环 false:非环
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
                    log.error("FeatureEntityGraph round the {} node is a cycle", child);
                    return true;
                }

                queue.offer(child);
                nodeColor.put(child, Color.GREY);
            }
        }
        return false;
    }

    private List<String> getRootNode(Map<String, FeatureEntity> featureEntityGraph) {
        return featureEntityGraph.values().stream()
                .filter(it -> it.getParents().isEmpty() || it.getStatus().get().isEndStates())
                .map(it -> it.getFeatureBean().getName())
                .collect(Collectors.toList());
    }

    enum Color {
        WHITE, GREY, BLACK
    }
}
