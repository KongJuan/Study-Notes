package com.kyn.jucLockTest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockDemo {
    public static void main(String[] args) {
        PhonePlus phonePlus = new PhonePlus();
        reentrantTest(phonePlus);
    }

    private static void syncTest(PhonePlus phonePlus) {
        new Thread(()->{
            phonePlus.sendSMS();
        }, "t1").start();
        new Thread(()->{
            phonePlus.sendEmail();
        }, "t2").start();
    }
    private static void reentrantTest(PhonePlus phonePlus) {
        new Thread(()->{
            phonePlus.method1();
        }, "t1").start();
        new Thread(()->{
            phonePlus.method1();
        }, "t2").start();
    }

}
class PhonePlus{

    //Synchronized TEST
    public synchronized  void sendSMS(){
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+"\t"+"sendSMS");
        sendEmail();
    }
    public synchronized void sendEmail(){
        System.out.println(Thread.currentThread().getName()+"\t"+"sendEmail");
    }
    //Reentrant TEST
    Lock lock=new ReentrantLock();
    public void method1(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getId() + "\t" +
                    "method1()");
            method2();
        } finally {
            lock.unlock();
        }
    }

    private void method2() {
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getId() + "\t" +
                    "method2()");
        } finally {
            lock.unlock();
        }
    }

}
