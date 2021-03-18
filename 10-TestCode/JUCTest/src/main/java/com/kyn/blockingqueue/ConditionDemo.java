package com.kyn.blockingqueue;

public class ConditionDemo {
    public static void main(String[] args) {
        ShareData shareData=new ShareData();
        new Thread(()->{
            for (int i = 1; i <10 ; i++) {
                shareData.printc1();
            }
        },"A").start();
        new Thread(()->{
            for (int i = 1; i <10 ; i++) {
                shareData.printc2();
            }
        },"B").start();
        new Thread(()->{
            for (int i = 1; i <10 ; i++) {
                shareData.printc3();
            }
        },"C").start();
    }
}
