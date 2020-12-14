package com.kyn.jucCasTest;

import java.util.concurrent.atomic.AtomicInteger;

public class CasTest02 {
    public static void main(String[] args) {
        AtomicInteger atomicInteger=new AtomicInteger(5);
        System.out.println(atomicInteger.compareAndSet(5, 2020)+"\t 当前数据值 : "+ atomicInteger.get());
        //修改失败
        System.out.println(atomicInteger.compareAndSet(5, 1024)+"\t 当前数据值 : "+ atomicInteger.get());
    }


}

class MyData{
    volatile int number = 0;
    AtomicInteger atomicInteger=new AtomicInteger();
    public void addPlusPlus(){
        number++;
    }
    public void addAtomic(){
        atomicInteger.getAndIncrement();
    }
    public void setTo60() {
        this.number = 60;
    }
}
