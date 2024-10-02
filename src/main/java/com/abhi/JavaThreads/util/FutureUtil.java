package com.abhi.JavaThreads.util;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class FutureUtil {


    public String getHello() {
        return "Hey there!!";
    }

    public String nameWithDelay() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Abhishek";
    }
    public void supplyAsync() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(this::nameWithDelay);

        // added callback, once my task will be done it will apply function using thenApply and then would print the string.
        completableFuture.thenApply((x) -> "Hello " + x)
                .thenAccept(System.out::println);

        // Resuming my other task meanwhile
        IntStream.range(0, 10).forEach(x -> {
            System.out.println("We are running the task no - " + x);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
