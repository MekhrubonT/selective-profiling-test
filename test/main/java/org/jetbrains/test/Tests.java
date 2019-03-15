package org.jetbrains.test;

import org.jetbrains.test.calltree.CallTree;
import org.jetbrains.test.calltree.FileParseException;
import org.jetbrains.test.calltree.aggregator.MethodCallAmount;
import org.jetbrains.test.calltree.aggregator.MethodCallTime;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Tests {
    @Before
    public void preparation() {
        CallTree.clear();
        Thread.currentThread().setName("Main thread");
    }

    @Test
    public void testCallAmount() {
        new MockApplication().a();

        CallTree instance = CallTree.getInstance();
        Map<String, Integer> actual = MethodCallAmount.execute(instance);
        Map<String, Integer> expected = new HashMap<>();
        expected.put("a", 1);
        expected.put("b", 1);
        expected.put("c", 2);
        expected.put("d", 4);
        expected.put("e", 40);
        expected.put(Thread.currentThread().getName() + " " + Thread.currentThread().getId(), 1);
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void testReadFromString() throws IOException, FileParseException {
        new MockApplication().a();
        CallTree real = CallTree.getInstance();
        System.out.println(real.toString());
        CallTree read = CallTree.readFromStream(
                new ByteArrayInputStream(real.toString().getBytes())
        );
        assertEquals(real, read);
    }

    @Test
    public void testTimeAmount() {
        new MockApplication().a();
        CallTree tree = CallTree.getInstance();
        Map<String, Long> callTime = MethodCallTime.execute(tree);
        System.out.println(callTime);
        assertTrue(callTime.get("a") >= callTime.get("b"));
        assertTrue(callTime.get("b") >= callTime.get("c"));
        assertTrue(callTime.get("d") >= callTime.get("e"));
    }
}
