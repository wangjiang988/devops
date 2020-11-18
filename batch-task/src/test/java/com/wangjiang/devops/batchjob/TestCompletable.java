package com.wangjiang.devops.batchjob;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TestCompletable {
    public static void main(String[] args) {
//        NormalTask task = new NormalTask();
//        CompletableFuture.runAsync(task).thenAccept(System.out::println);

        CallableTask task = new CallableTask();
        CompletableFuture.supplyAsync(task).thenAccept(System.out::println);
    }

    public static class NormalTask implements Runnable {

        @Override
        public void run() {
            System.out.println("###");
        }
    }

    public static class CallableTask implements Supplier<String> {

        @Override
        public String get() {
            return "hi its me";
        }
    }
}
