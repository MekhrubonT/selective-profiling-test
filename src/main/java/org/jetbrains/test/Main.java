package org.jetbrains.test;

import org.jetbrains.test.calltree.CallTree;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            int start = 100 * i;
            List<String> arguments = IntStream.range(start, start + 10)
                    .mapToObj(Integer::toString)
                    .collect(Collectors.toList());

            service.submit(() -> new DummyApplication(arguments).start());
        }
        service.shutdown();
        while (!service.isTerminated()) {
            try {
                service.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                ignored.printStackTrace();
            }
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out))) {
            CallTree.getTrees().forEach(writer::println);
        }
        for (CallTree bypassTree : CallTree.getTrees()) {
            try {
                bypassTree.storeInFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
