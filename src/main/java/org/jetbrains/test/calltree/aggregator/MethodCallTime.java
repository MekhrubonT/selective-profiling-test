package org.jetbrains.test.calltree.aggregator;

import org.jetbrains.test.calltree.CallTree;

import java.util.HashMap;
import java.util.Map;

public class MethodCallTime {
    public static Map<String, Long> execute(CallTree tree) {
        HashMap<String, Long> res = new HashMap<>();
        tree.forEach(node -> res.merge(node.getFunctionName(),
                node.executionTime(),
                (vOld, delta) -> vOld + delta));
        return res;
    }
}
