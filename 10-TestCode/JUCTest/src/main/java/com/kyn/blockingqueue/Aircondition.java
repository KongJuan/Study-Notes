package com.kyn.blockingqueue;

public class Aircondition {
    private int num=0;
    public synchronized void increment() throws InterruptedException {
        if (num!=0){
            this.wait();
        }
        num++;
        System.out.println(Thread.currentThread().getName()+" \t "+num);
        this.notifyAll();
    }
    public synchronized void decrement() throws InterruptedException {
        if (num==0){
            this.wait();
        }
        num--;
        System.out.println(Thread.currentThread().getName()+" \t "+num);
        this.notifyAll();
    }
}
