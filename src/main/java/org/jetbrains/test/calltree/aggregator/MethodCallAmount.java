package org.jetbrains.test.calltree.aggregator;

import org.jetbrains.test.calltree.CallTree;

import java.util.HashMap;
import java.util.Map;

public class MethodCallAmount {
    public static Map<String, Integer> execute(CallTree tree) {
        HashMap<String, Integer> res = new HashMap<>();
        tree.forEach(node -> res.merge(node.getFunctionName(), 1,
                (k, v) -> k + v
        ));
        return res;
    }
}
