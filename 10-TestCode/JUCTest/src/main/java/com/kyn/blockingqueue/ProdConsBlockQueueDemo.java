package com.kyn.blockingqueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ProdConsBlockQueueDemo {
    public static void main(String[] args) {
        MyResource myResource = new MyResource(new ArrayBlockingQueue<>(5));
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t⽣产线程启动");
            try {
                myResource.myProd();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "prod-1").start();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t⽣产线程启动");
            try {
                myResource.myProd();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "prod-2").start();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t消费线程启动");
            try {
                myResource.myCons();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "cons-1").start();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t消费线程启动");
            try {
                myResource.myCons();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "cons-2").start();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("5秒钟后，叫停");
        myResource.stop();
    }
}
