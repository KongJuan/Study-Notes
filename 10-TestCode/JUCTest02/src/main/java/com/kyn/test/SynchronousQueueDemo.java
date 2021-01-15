package com.kyn.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class SynchronousQueueDemo {
    public static void main(String[] args) {
        BlockingQueue<String> blockingQueue=new SynchronousQueue<String>();
        new Thread(()->{
            try {
                System.out.println(Thread.currentThread().getName()+"\tput 1");
                blockingQueue.put("1");

                System.out.println(Thread.currentThread().getName()+"\tput 2");
                blockingQueue.put("2");

                System.out.println(Thread.currentThread().getName()+"\tput 3");
                blockingQueue.put("3");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"AAA").start();
        new Thread(()->{

            try {

                try{ TimeUnit.SECONDS.sleep(5); }catch
                (InterruptedException e){ e.printStackTrace(); }
                System.out.println(Thread.currentThread().getName()+"\ttake "+blockingQueue.take());

                try{ TimeUnit.SECONDS.sleep(5); }catch
                (InterruptedException e){ e.printStackTrace(); }

                System.out.println(Thread.currentThread().getName()+"\ttake "+blockingQueue.take());

                try{ TimeUnit.SECONDS.sleep(5); }catch
                (InterruptedException e){ e.printStackTrace(); }

                System.out.println(Thread.currentThread().getName()+"\ttake"+blockingQueue.take());
            } catch (Exception e) {

                e.printStackTrace();
            }
        },"BBB").start();


    }
}
