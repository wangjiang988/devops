package com.wangjiang.devops.batchtask.test.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ThreadB implements Runnable {
    private List<String> list = new ArrayList<>();

    @Override
    public void run() {
        // 加了final 多少个线程输 list就是多少个
//        final List<String> list = this.list;
        List<String> list = this.list;
        list.add("it's from b");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        println(list);
    }

    private void println(List<String> list) {
        System.out.println(list);
    }
}
