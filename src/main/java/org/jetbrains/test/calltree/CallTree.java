package org.jetbrains.test.calltree;

import javafx.util.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.jetbrains.test.calltree.utils.Utils.*;

/**
 * This tree is used to profile methods. It allowes to register the starting and finishing method call
 * and build the calls tree.
 * Created by mekhrubon on 09.05.2017.
 */
public class CallTree {
    public static final int DEFAULT_SHIFT = 3;
    private static final Map<Thread, CallTree> trees = new ConcurrentHashMap<>();
    private final Node root;
    private final Stack<Node> currentTreeStackTrace = new Stack<>();


    /**
     * Creates in instance of CallTree with given name for the root vertex with default shift.
     *
     * @param rootName name of root vertex
     */
    public CallTree(String rootName) {
        this(rootName, DEFAULT_SHIFT);
    }

    // Constructor with the shift of nested call in output relatively to calling method, throws illegalArgumentException
    // if shift is negative
    public CallTree(String rootName, int shift) {
        this(new Node(rootName, shift));
    }

    private CallTree(Node root) {
        this.root = root;
        currentTreeStackTrace.add(root);
    }

    // clean all information about trees
    public static void clear() {
        trees.clear();
    }

    /**
     * Returns the list of all CallTree created by {@link CallTree#getInstance()}.
     *
     * @return the list of all CallTree created by {@link CallTree#getInstance()}.
     */
    public static List<CallTree> getTrees() {
        return new ArrayList<>(trees.values());
    }

    /**
     * During the first call of this method in this thread creates the instance of CallTree and returns it. For every
     * next call of this method returns the previously created CallTree object.
     *
     * @return the appropriate to current thread object of CallTree
     */
    public static CallTree getInstance() {
        trees.putIfAbsent(Thread.currentThread(), new CallTree(Thread.currentThread().getName() + " " + Thread.currentThread().getId()));
        return trees.get(Thread.currentThread());
    }

    // Returns parsed CallTree from given file
    public static CallTree readFromFile(String pathname) throws IOException, FileParseException {
        return new CallTree(recursiveRead(new BufferedReader(new InputStreamReader(new FileInputStream(pathname), StandardCharsets.UTF_8)), null));
    }

    // the helper function for parsing tree from file.
    private static Node recursiveRead(BufferedReader reader, String expectedTitle) throws IOException, FileParseException {
        String header = reader.readLine();
        int startIndexOfTitleFunctionBlock = header.indexOf("[");
        int endIndexOfTitleFunctionBlock = header.indexOf("]:");
        String headerWithoutParenthethis = header
                .substring(startIndexOfTitleFunctionBlock + 1, endIndexOfTitleFunctionBlock);

        if (expectedTitle != null && !headerWithoutParenthethis.equals(expectedTitle)) {
            throw new FileParseException("The given file is corrupted. Title expected ["
                    + expectedTitle + "], but was found [" + headerWithoutParenthethis + "]");
        }

        Pair<String, Integer> parsedTitleNameAndHash = parseNode(headerWithoutParenthethis);
        Node node = new Node(parsedTitleNameAndHash.getKey(), 2);
        List<String> nestedCalledMethodsList = methodsCallFromNodeParser(header.substring(endIndexOfTitleFunctionBlock + 2));

        for (String s : nestedCalledMethodsList) {
            node.addChilder(recursiveRead(reader, s));
        }
        if (node.hashCode() != parsedTitleNameAndHash.getValue()) {
            throw new FileParseException("An error ocurred while parsing tree, Hashcodes are not equal.");
        }
        return node;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CallTree)) {
            return false;
        }
        return root.equals(((CallTree) obj).root) && currentTreeStackTrace.equals(((CallTree) obj).currentTreeStackTrace);
    }

    @Override
    public int hashCode() {
        return (root.hashCode() * 2957) ^ currentTreeStackTrace.hashCode();
    }

    private String treeThreadName() {
        return root.name;
    }


    /**
     * Returns a string representation of this CallTree.  The first line of string
     * representation consists name of thread, where this CallTree was created(not deserialized) and code number for this vertex,
     * enclosed in square brackets (<tt>"[]"</tt>). Then pairs of all registered methods called in this thread and their code numbers
     * enclosed in round brackets (<tt>"()"</tt>). On the next lines this toString method is called recursively on all
     * aforementioned methods, but instead of thread name method name is placed.
     * <p>
     * Code numbers of two vertex can coincide and that is very be sad.
     *
     * @return a string representation of this CallTree
     */

    // The code number are making a great overhead on generating the resulting String, but make the navigation easier.
    @Override
    public String toString() {
        return root.print(new StringBuilder(), 0).toString();
    }

    /**
     * Calls {@link CallTree#storeInFile(String)} with name argument {@code thread.getName() + " " + thread.getId() + ".tree"},
     * where thread is one, in which this instance was created, but not deserialized.
     * <p>
     * {@link CallTree#storeInFile(String)}
     */
    public void storeInFile() throws IOException {
        storeInFile(treeThreadName() + ".tree");

    }

    /**
     * Tries to store this tree in the given file.
     *
     * @throws InvalidPathException - if the path string cannot be converted to a Path
     * @throws SecurityException    In the case of the default provider, the {@link
     *                              SecurityManager#checkRead(String)} is invoked to check
     *                              read access to the file.
     * @see Files#createDirectories(Path, FileAttribute[])
     * @see Files#createFile(Path, FileAttribute[])
     */
    public void storeInFile(String name) throws IOException {
        createDirectoriesAndFile(name);
        try (PrintWriter writer = new PrintWriter(new File(name))) {
            writer.println(toString());
        }
    }


    /**
     * Registers method call to this CallTree
     *
     * @param methodName name of called method
     */
    public void addMethodCall(String methodName, Object... args) {
        Node currentState = currentTreeStackTrace.peek();
        currentTreeStackTrace.push(currentState.addChildren(methodName + "{" + String.join(",", Arrays.stream(args).map(Object::toString).toArray(String[]::new)) + "}"));
    }

    /**
     * Registers the finish of the last unfinished called method.
     */
    public void finishMethodCall() {
        if (currentTreeStackTrace.size() == 1) {
            throw new IllegalStateException("Trying to finish last registered method, but hadn't added it before");
        }
        currentTreeStackTrace.pop();
    }

    static private class Node {
        final String name;
        final List<Node> children;
        String shift;

        Node(String name, int shift) {
            if (shift < 0) {
                throw new IllegalArgumentException("Shift should be non-negative");
            }

            this.name = name;
            children = new ArrayList<>();

            StringBuilder d = new StringBuilder();
            d.append("|");
            while (--shift > 0) {
                d.append(" ");
            }
            this.shift = d.toString();
        }

        Node addChildren(String name) {
            return addChilder(new Node(name, shift.length()));
        }

        private Node addChilder(Node newChild) {
            children.add(newChild);
            return newChild;
        }

        @Override
        public int hashCode() {
            return (name.hashCode() * 1367) ^ children.hashCode();
        }

        @Override
        public String toString() {
            return print(new StringBuilder(), 0).toString();
        }

        StringBuilder print(StringBuilder result, int depth) {
            for (int i = 0; i < depth; i++) {
                result.append(shift);
            }
            result.append("[").append(name).append(", ").append(hashCode()).append("]: ");
            for (int i = 0; i < children.size(); i++) {
                Node child = children.get(i);
                result.append("(").append(child.name).append(", ").append(child.hashCode()).append(")");
                if (i + 1 != children.size()) {
                    result.append(", ");
                }
            }
            result.append("\n");
            for (Node child : children) {
                child.print(result, depth + 1);
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Node)) {
                return false;
            }
            return name.equals(((Node) obj).name) && children.equals(((Node) obj).children);
        }
    }

}
