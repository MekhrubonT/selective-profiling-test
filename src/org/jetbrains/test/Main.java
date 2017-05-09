package org.jetbrains.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws InterruptedException, IOException {
        ExecutorService service = Executors.newFixedThreadPool(3);
        for(int i = 0; i < 5; i++) {
            int start = 100 * i;
            List<String> arguments = IntStream.range(start, start + 10)
                    .mapToObj(Integer :: toString)
                    .collect(Collectors.toList());
            Runnable r = () -> new DummyApplication(arguments).start();
                        service.submit(r);
        }
        service.shutdown();
        service.awaitTermination(1000, TimeUnit.HOURS);
        System.out.println("Shutdown");

        try (PrintWriter writer = new PrintWriter(new File("output.txt"))) {
            CallTree.getTrees().forEach(writer::println);
        }

        for (CallTree bypassTree : CallTree.getTrees()) {
            bypassTree.storeInFile();
        }
    }
}
