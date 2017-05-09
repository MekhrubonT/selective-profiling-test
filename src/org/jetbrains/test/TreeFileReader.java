package org.jetbrains.test;

import java.io.IOException;

/**
 * Created by mekhrubon on 09.05.2017.
 */
public class TreeFileReader {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        for (String arg : args) {
            System.out.println(CallTree.readFromFile(arg));
        }
    }

}
