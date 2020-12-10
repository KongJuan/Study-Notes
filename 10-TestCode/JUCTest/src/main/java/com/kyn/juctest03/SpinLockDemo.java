package com.kyn.juctest03;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class SpinLockDemo {

    public static void main(String[] args) {
        SpinLockDemo spinLockDemo=new SpinLockDemo();
        new Thread(()->{
            spinLockDemo.myLock();
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spinLockDemo.myUnlock();
        },"AA").start();
        new Thread(()->{
            spinLockDemo.myLock();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            spinLockDemo.myUnlock();
        },"BB").start();
    }
    AtomicReference<Thread> atomicReference=new AtomicReference<>();
    public void myLock(){
        Thread thread=Thread.currentThread();
        System.out.println(Thread.currentThread().getName()+"\t"+"come in...");
        while(!atomicReference.compareAndSet(null,thread));
    }
    public void myUnlock(){
        Thread thread=Thread.currentThread();
        System.out.println(Thread.currentThread().getName()+"\t"+"unlock");
        atomicReference.compareAndSet(thread,null);
    }
}
