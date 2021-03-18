package com.kyn.blockingqueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ShareData {
    private int number=1;
    private Lock lock=new ReentrantLock();
    private Condition c1=lock.newCondition();
    private Condition c2=lock.newCondition();
    private Condition c3=lock.newCondition();

    public void printc1() {
        try{
            lock.lock();
            while(number!=1){
                c1.await();
            }
            for (int i = 1; i <=5 ; i++) {
                System.out.println(Thread.currentThread().getName()+"\t"+i);
            }
            number=2;
            c2.signal();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public void printc2() {
        try{
            lock.lock();
            while(number!=2){
                c2.await();
            }
            for (int i = 1; i <=5 ; i++) {
                System.out.println(Thread.currentThread().getName()+"\t"+i);
            }
            number=3;
            c3.signal();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
    public void printc3() {
        try{
            lock.lock();
            while(number!=3){
                c3.await();
            }
            for (int i = 1; i <=5 ; i++) {
                System.out.println(Thread.currentThread().getName()+"\t"+i);
            }
            number=1;
            c1.signal();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }
}
