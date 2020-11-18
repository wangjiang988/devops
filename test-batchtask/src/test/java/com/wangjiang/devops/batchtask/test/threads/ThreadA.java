package com.wangjiang.devops.batchtask.test.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ThreadA implements Runnable {

    @Override
    public void run() {
        // 这种写法不可取，final变量虽然是共享的，但是都是new的对象，并没有共享 #wangjiang 2020-11-18 05:16:01#
        final List<String> list = new ArrayList<>();
        list.add("it's from a");
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
