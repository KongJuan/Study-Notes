package com.kyn.jucVolatileTest;

import java.util.concurrent.atomic.AtomicInteger;

public class VolatileDemo02 {
    public static void main(String[] args) {
        atomicDemo01();
    }
    //通过加锁保证原子性
    private static void atomicDemo01(){
        System.out.println("原子性测试");
        MyData02 data02=new MyData02();
        for (int i = 1; i <=20; i++) {
            new Thread(()->{
                for (int j = 0; j <1000 ; j++) {
                    synchronized (VolatileDemo02.class){
                        data02.setNum();
                    }

                }
            },String.valueOf(i)).start();
        }
        while(Thread.activeCount()>2){
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName()+"\t int类型最终number值: "+data02.num);

    }

    //使用AtomicInteger保证原子性
    private  static void atomicDemo02(){
        System.out.println("原子性测试");
        MyData02 data02=new MyData02();
        for (int i = 1; i <=20; i++) {
            new Thread(()->{
                for (int j = 0; j <1000 ; j++) {
                    data02.addAtomic();
                }
            },String.valueOf(i)).start();
        }
        while(Thread.activeCount()>2){
            Thread.yield();
        }
        System.out.println(Thread.currentThread().getName()+"\t int类型最终number值: "+data02.num);
    }
}

class MyData02{
    volatile int num=0;
    AtomicInteger atomicInteger=new AtomicInteger();
    //此时num前面加了volatile，当不能保证原子性
    public void setNum(){
        num++;
    }
    public void addAtomic(){
        atomicInteger.getAndIncrement();
    }
}