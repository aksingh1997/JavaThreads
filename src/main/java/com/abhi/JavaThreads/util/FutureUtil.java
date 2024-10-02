package com.abhi.JavaThreads.util;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class FutureUtil {


    public String getHello() {
        return "Hey there!!";
    }
    /*
    Gyaan -
        1. What is future -> whenever we submit our task as runnable, callable or any other way to a pool of thread, the tasks are picked by one of the threads in pool
        and execution starts. But we don't know which task's execution in the pool will start at what time and when it will be completed. To keep a track on this,
        we can wrap our task with Future. With future's isDone() we can find if the task is completed. With future's get(), we can block current thread operation and
        force the task to be completed.
        2. Disadvantage of future -> We cannot add a callback to our future, lets say when a task is completed, come back and perform certain tasks. We cannot chain
        different futures together.
        3. CompletableFuture -> This came to overcome disadvantages of future. We can add callback to completable future. And we can chain them too.
        4. it used supplyAsync(Supplier) and runAsync(Runnable) to perform its task, by default uses ForkJoinPool.commonPoolThread, but we can provide our thread pool too.
        5. Three callback functions are =
            a. CompletableFuture<T> thenApply(Function<outputOfFuture, T>)
            b. CompletableFuture<Void> thenAccept(Function<outputOfFuture>, void)
            c. void thenRun(Function<outputOfFuture>, void)
        6. callbacks like thenApplyAsync, thenAcceptAsync, thenRunAsync--> they consume thread from thread pool again instead of current thread to perform callback operation

     */

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
                .thenAccept(System.out::println)
                .thenRun(() -> System.out.println("I have completed the task from the future"));

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


    public void combineCompletableFuture() {
        CompletableFuture<Double> getHeight = CompletableFuture.supplyAsync(() -> {
            // assume sleep is equivalent to fetching data from db
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int height = 174;
            System.out.println("height= " + height);
            return (double)height / 100;
        });

        CompletableFuture<Double> getWeight = CompletableFuture.supplyAsync(() -> {
            // assume sleep is equivalent to fetching data from db
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int weight = 68;
            System.out.println("weight= " + weight);
            return (double)weight;
        });

        // Combine both futures and use their results as input to another CompletableFuture
        CompletableFuture<Double> getBmi = getWeight.thenCombine(getHeight, (x, y) -> x / (y * y)); // here x = result of getHeight, y = result of getWeight
        getBmi.thenAccept((x) -> System.out.println("Total bmi= " + x));

        // this is equivalent to get() in Future. Forceful execution
        getBmi.join();
    }
}
