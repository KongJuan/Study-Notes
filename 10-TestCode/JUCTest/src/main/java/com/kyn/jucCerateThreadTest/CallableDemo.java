package com.kyn.jucCerateThreadTest;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class CallableDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask=new FutureTask<>(new MyThread());
        new Thread(futureTask,"AA").start();
        int result1=100;
        int result2=futureTask.get();
        System.out.println("result=" + (result1 + result2));
    }
}
class MyThread implements Callable<Integer>{

    @Override
    public Integer call() throws Exception {
        System.out.println("callable come in...");
        TimeUnit.SECONDS.sleep(3);
        return 1024;
    }
}
