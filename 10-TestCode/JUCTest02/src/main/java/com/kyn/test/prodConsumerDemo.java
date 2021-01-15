package com.kyn.test;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产者消费者模式
 */
public class prodConsumerDemo {
    public static void main(String[] args) {
        version2Test();
    }

    //版本一测试
    public static void version1Test(){
        SynVersion synVersion=new SynVersion();
        new Thread(()->{
            for (int i = 1; i <=2 ; i++) {
                try {
                    synVersion.increament();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for (int i = 1; i <=2 ; i++) {
                try {
                    synVersion.decreament();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"B").start();
        new Thread(()->{
            for (int i = 1; i <=2 ; i++) {
                try {
                    synVersion.increament();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"C").start();
        new Thread(()->{
            for (int i = 1; i <=2 ; i++) {
                try {
                    synVersion.decreament();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"D").start();
    }

    //版本二测试
    public static void version2Test(){
        LockVersion synVersion=new LockVersion();
        new Thread(()->{
            for (int i = 1; i <=2 ; i++) {
                try {
                    synVersion.increament();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for (int i = 1; i <=2 ; i++) {
                try {
                    synVersion.decreament();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"B").start();
        new Thread(()->{
            for (int i = 1; i <=2 ; i++) {
                try {
                    synVersion.increament();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"C").start();
        new Thread(()->{
            for (int i = 1; i <=2 ; i++) {
                try {
                    synVersion.decreament();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },"D").start();
    }
}



//版本一
class SynVersion{
    private int num=0;
    public synchronized void increament() throws InterruptedException {
        while(num!=0){
            this.wait();
        }
        num++;
        System.out.println(Thread.currentThread().getName()+"\t"+num);
        this.notifyAll();
    }
    public synchronized void decreament() throws InterruptedException {
        while(num==0){
            this.wait();
        }
        num--;
        System.out.println(Thread.currentThread().getName()+"\t"+num);
        this.notifyAll();
    }
}
class LockVersion{
    private int num=0;
    private Lock lock=new ReentrantLock();
    private Condition condition=lock.newCondition();
    public void increament() throws InterruptedException {
        lock.lock();
        try{
            while(num!=0){
                condition.await();
            }
            num++;
            System.out.println(Thread.currentThread().getName()+"\t"+num);
            condition.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public synchronized void decreament() throws InterruptedException {
        lock.lock();
        try{
            while(num==0){
                condition.await();
            }
            num--;
            System.out.println(Thread.currentThread().getName()+"\t"+num);
            condition.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
