package com.wangjiang.devops.batchjob;

import java.util.concurrent.TimeUnit;

public class TestThread {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println(Runtime.getRuntime().availableProcessors()/2);
        long nanoTime = System.nanoTime();

        TimeUnit.SECONDS.sleep(1);

        long nanoTime2 = System.nanoTime();
        System.out.println(nanoTime);
        System.out.println(nanoTime2);


        long convert = TimeUnit.NANOSECONDS.convert(3600,
                TimeUnit.MILLISECONDS);
        System.out.println(convert);
    }
}
