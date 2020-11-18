package com.wangjiang.devops.batchtask.test;

import com.wangjiang.devops.batchtask.test.threads.ThreadA;
import com.wangjiang.devops.batchtask.test.threads.ThreadB;

import java.util.concurrent.TimeUnit;

public class TestThread {
    public static void main(String[] args) throws InterruptedException {
//        ThreadA aa =  new ThreadA();
//        Thread a = new Thread(aa);
//        Thread b = new Thread(aa);
//        a.start();
//        b.start();

        ThreadB aa =  new ThreadB();
        Thread a = new Thread(aa);
        Thread b = new Thread(aa);
        a.start();
        b.start();
        TimeUnit.SECONDS.sleep(10);
    }
}
