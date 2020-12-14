package com.kyn.jucAuxiliaryTest;

import com.sun.jmx.snmp.ThreadContext;

import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch主要有两个方法，当一个或多个线程调用await方法时，这些线程会阻塞。
 * 其他线程调用countDown方法会将计时器减1（调用countDown方法的线程不会阻塞），
 * 当计数器的值变为0时，因await方法阻塞的线程会被唤醒，继续执行。
 */
public class CountDownLatchDemo {
    //案例：main线程是班⻓，6个线程是学⽣。只有6个线程运⾏完毕，都离开教室后，main线程班⻓才会关教室⻔。
    public static void main(String[] args) {
        CountDownLatch countDownLatch=new CountDownLatch(6);

        for (int i = 1; i <=6 ; i++) {
            new Thread(()->{
                System.out.println(Thread.currentThread().getName()+"\t"+"离开教室");
                countDownLatch.countDown();
            },String.valueOf(i)).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+"\t"+"关教室门");
    }
}
