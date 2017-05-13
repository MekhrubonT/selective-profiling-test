package org.jetbrains.test;

import org.jetbrains.test.calltree.CallTree;
import org.jetbrains.test.calltree.FileParseException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by mekhrubon on 09.05.2017.
 */
public class TreeFileReader {
    public static void main(String[] args) throws IOException, FileParseException {
        PrintWriter writer = new PrintWriter(new File("output.txt"));
        writer.println(CallTree.readFromFile("D:\\selective-profiling-test\\pool-1-thread-1 11.tree"));
        writer.close();
    }

}
