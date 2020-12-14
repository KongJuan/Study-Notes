package com.kyn.jucAuxiliaryTest;

import java.util.concurrent.CyclicBarrier;

/**
 * CyclicBarrier和CountDownLatch不同之处：
 * 1、CyclicBarrier只能唤起一个任务，CountDownLatch可以唤起多个任务。
 * 2、CyclicBarrier可重用，CountDownLatch不可重用，计数值为0该CountDownLatch就不可再用了。
 */
public class CyclicBarrierDemo {
    //超市购物，每次购物积分+1，10积分会领取一份超级礼物
    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier=new CyclicBarrier(10,()->{
            System.out.println("========领取成功");
        });
        for (int i = 1; i <=10 ; i++) {
            int tempInt = i;
            new Thread(()->{
                System.out.println(Thread.currentThread().getName() + "\t积分" + tempInt);
                try {
                    cyclicBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, String.valueOf(i)).start();
        }

    }
}
