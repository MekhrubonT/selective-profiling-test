package org.jetbrains.test.calltree.utils;

import javafx.util.Pair;
import org.jetbrains.test.calltree.FileParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mekhrubon on 12.05.2017.
 */
public class Utils {
    public static void createDirectoriesAndFile(String name) throws IOException {
        Path path = Paths.get(name);
        if (Files.notExists(path)) {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
        }
    }

    public static List<String> methodsCallFromNodeParser(String calledMethodsStringForm) {
        int balance = 0, last = 0;
        List<String> nodes = new ArrayList<>();
        for (int i = 0; i < calledMethodsStringForm.length(); ++i) {
            switch (calledMethodsStringForm.charAt(i)) {
                case '(':
                    if (balance == 0) {
                        last = i;
                    }
                    balance++;
                    break;
                case ')':
                    balance--;
                    if (balance == 0) {
                        nodes.add(calledMethodsStringForm.substring(last + 1, i));
                    }
                    break;
            }
        }
        return nodes;
    }

    public static Pair<String, Integer> parseNode(String nameComaHashNode) throws FileParseException {
        for (int i = 0; i < nameComaHashNode.length(); i++) {
            if (nameComaHashNode.charAt(i) == ',' && isInteger(nameComaHashNode.substring(i + 2))) {
                return new Pair<>(nameComaHashNode.substring(0, i), Integer.parseInt(nameComaHashNode.substring(i + 2)));
            }
        }
        throw new FileParseException("The given file doesn't contain tree in required format");
    }

    private static boolean isInteger(String isinteger) {
        try {
            int value = Integer.parseInt(isinteger);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
