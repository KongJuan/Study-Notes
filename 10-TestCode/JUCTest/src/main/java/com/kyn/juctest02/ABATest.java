package com.kyn.juctest02;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class ABATest {
    static AtomicReference atomicReference=new AtomicReference(100);
    static AtomicStampedReference<Integer> stampedReference=new AtomicStampedReference<>(100,1);

    public static void main(String[] args) {

        System.out.println("=========ABA问题的产生==========");

        new Thread(()->{
            atomicReference.compareAndSet(100,101);
            atomicReference.compareAndSet(101,100);
        },"t1").start();

        new Thread(()->{
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(atomicReference.compareAndSet(100, 2020) +
                    "\t" + atomicReference.get().toString());

        },"t2").start();

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("========ABA问题的解决========");

        new Thread(()->{
            System.out.println(Thread.currentThread().getName()+"开始版本号为"+stampedReference.getStamp());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stampedReference.compareAndSet(100,101,stampedReference.getStamp(),stampedReference.getStamp()+1);
            System.out.println(Thread.currentThread().getName()+"第一次版本号为"+stampedReference.getStamp());
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stampedReference.compareAndSet(101,100,stampedReference.getStamp(),stampedReference.getStamp()+1);
            System.out.println(Thread.currentThread().getName()+"第二次版本号为"+stampedReference.getStamp());
        },"t3").start();

        new Thread(() -> {

            int stamp = stampedReference.getStamp();

            System.out.println(Thread.currentThread().getName() + "\t第⼀次版本号： " + stamp);

            try { TimeUnit.SECONDS.sleep(3); } catch (InterruptedException
                    e) { e.printStackTrace(); }

            boolean result=stampedReference.compareAndSet(100,2020,

                    stamp,stamp+1);

            System.out.println(Thread.currentThread().getName()+"\t修改成功与否："+result+" 当前最新版本号"+stampedReference.getStamp());

                    System.out.println(Thread.currentThread().getName()+"\t当前实际值："+stampedReference.getReference());
        }, "t4").start();

    }
}
