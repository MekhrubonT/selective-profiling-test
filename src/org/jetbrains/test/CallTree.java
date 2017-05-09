package org.jetbrains.test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This tree is used to profile methods. It allowes to register the starting and finishing method call
 * and build the calls tree.
 * Created by mekhrubon on 09.05.2017.
 */
public class CallTree {
    private static final Map<Thread, CallTree> trees = new ConcurrentHashMap<>();
    private final Node root;
    private final Stack<Node> currentTreeStackTrace = new Stack<>();


    /**
     * Creates in instance of CallTree with given name for the root vertex.
     * @param rootName name of root vertex
     */
    public CallTree(String rootName) {
        this(new Node(rootName));
    }

    private CallTree(Node root) {
        this.root = root;
        currentTreeStackTrace.add(root);
    }


    /**
     * Returns the list of all CallTree created by {@link CallTree#getInstance()}.
     * @return the list of all CallTree created by {@link CallTree#getInstance()}.
     */
    public static List<CallTree> getTrees() {
        return new ArrayList<>(trees.values());
    }

    /**
     * During the first call of this method in this thread creates the instance of CallTree and returns it. For every
     * next call of this method returns the previously created CallTree object.
     * @return the appropriate to current thread object of CallTree
     */
    public static CallTree getInstance() {
        trees.putIfAbsent(Thread.currentThread(), new CallTree(Thread.currentThread().getName() + " " + Thread.currentThread().getId()));
        return trees.get(Thread.currentThread());
    }

    /**
     * Tries to deserialize previously stored CallTree from the given file.
     * @param name the relative or absolute path to required file
     * @return constructed CallTree from given file
     * @throws FileNotFoundException if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     * @throws IOException if an I/O error occurs while opening or reading the file
     * @throws ClassNotFoundException Class of a Node cannot be found.
     * @throws InvalidClassException Something is wrong with a class Node while trying to deserialize.
     * @throws SecurityException      if a security manager exists and its
     *               <code>checkRead</code> method denies read access
     *               to the file.
     */
    public static CallTree readFromFile(String name) throws IOException, ClassNotFoundException {
        try ( FileInputStream fileIn = new FileInputStream(name);
             ObjectInputStream inputStream = new ObjectInputStream(fileIn)) {
            return new CallTree((Node) inputStream.readObject());
        }
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
     *
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
     * Calls {@link CallTree#storeInFile(String)} with name argument {@code thread.getName() + " " + thread.getId()},
     * where thread is one, in which this instance was created, but not deserialized.
     * @throws IOException {@link CallTree#storeInFile(String)}
     */
    public void storeInFile() throws IOException {
        storeInFile(treeThreadName() + ".tree");
    }

    /**
     * Tries to store this tree in the given file.
     * @param name relative or absolute path to file. If no such file found, this method will try to create it.
     * @throws InvalidPathException if the name cannot be converted to a {@code Path}
     * @throws IOException if file cannot be opened, created or directories cannot be created.
     * @throws SecurityException  if a security manager exists and its
      *               <code>checkWrite</code> method denies write access
      *               to the file.
     * @throws  InvalidClassException Something is wrong with a {@link Node} during
     *          serialization.
     * @throws  NotSerializableException Some object to be serialized does not
     *          implement the java.io.Serializable interface.

     * @see Files#createDirectories(Path, FileAttribute[])
     * @see Files#createFile(Path, FileAttribute[])
     */
    public void storeInFile(String name) throws IOException {
        Path path = Paths.get(name);
        if (Files.notExists(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
        }
        try (FileOutputStream fileOut = new FileOutputStream(name);
             ObjectOutputStream outputStream = new ObjectOutputStream(fileOut)) {
            outputStream.writeObject(root);
            System.out.println("Successfuly stored " + treeThreadName() + " in " + name);
        }
    }


    /**
     * Registers method call to this CallTree
     * @param methodName name of called method
     */
    public void addMethodCall(String methodName) {
        Node currentState = currentTreeStackTrace.peek();
        currentTreeStackTrace.push(currentState.addChildren(methodName));
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

    static private class Node implements Serializable {
        final String name;
        final List<Node> children;

        Node(String name) {
            this.name = name;
            children = new ArrayList<>();
        }

        Node addChildren(String name) {
            Node newChild = new Node(name);
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
                result.append(' ');
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
    }

}
