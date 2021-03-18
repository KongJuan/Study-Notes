package com.kyn.blockingqueue;

public class ProdConsumerDemo {
    public static void main(String[] args) {
        Aircondition aircondition = new Aircondition();
        new Thread(()->{
            for (int i = 1; i <= 10; i++) {
                try {
                    aircondition.increment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"A").start();
        new Thread(()->{
            for (int i = 1; i <= 10; i++) {
                try {
                    aircondition.decrement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"B").start();

        new Thread(()->{
            for (int i = 1; i <= 10; i++) {
                try {
                    aircondition.increment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"C").start();
        new Thread(()->{
            for (int i = 1; i <= 10; i++) {
                try {
                    aircondition.decrement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"D").start();
    }
}
