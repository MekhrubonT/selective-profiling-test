package org.jetbrains.test;


import java.util.List;
import java.util.Random;

/**
 * Nikolay.Tropin
 * 18-Apr-17
 */
public class DummyApplication {
    private final CallTree tree;

    private final List<String> args;
    private Random random = new Random(System.nanoTime());


    public DummyApplication(List<String> args) {
        this.args = args;
        tree = CallTree.getInstance();
    }

    private boolean nextBoolean() {
        return random.nextBoolean();
    }

    private boolean stop() {
        return random.nextDouble() < 0.05;
    }

    private String nextArg() {
        int idx = random.nextInt(args.size());
        return args.get(idx);
    }

    private void sleep() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException ignored) {

        }
    }

    private void abc(String s) {
        tree.addMethodCall("abc");

        try {
            sleep();
            if (stop()) {
                //do nothing
            } else if (nextBoolean()) {
                def(nextArg());
            } else {
                xyz(nextArg());
            }
        } finally {
            tree.finishMethodCall();
        }
    }

    private void def(String s) {
        tree.addMethodCall("def");
        try {
            sleep();
            if (stop()) {
                //do nothing
            } else if (nextBoolean()) {
                abc(nextArg());
            } else {
                xyz(nextArg());
            }
        } finally {
            tree.finishMethodCall();
        }
    }

    private void xyz(String s) {
        tree.addMethodCall("xyz");
        try {
            sleep();
            if (stop()) {
                //do nothing
            } else if (nextBoolean()) {
                abc(nextArg());
            } else {
                def(nextArg());
            }
        } finally {
            tree.finishMethodCall();
        }
    }

    public void start() {
        abc(nextArg());
    }
}
