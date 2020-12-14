package com.kyn.jucLockTest;

import java.util.concurrent.TimeUnit;

public class LockDemo {
    public static void main(String[] args) {
        Phone phone1=new Phone();
        Phone phone2=new Phone();

        new Thread(()->{
            phone1.sendEmail();
        },"A").start();

        try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException e) {e.printStackTrace(); }

        new Thread(()->{
            phone2.sendMessage();
        },"B").start();

        /*new Thread(()->{
            phone1.hello();
        },"C").start();*/

    }
}
class Phone{
    public static synchronized void sendEmail(){
        try { TimeUnit.SECONDS.sleep(4); } catch (InterruptedException e) {e.printStackTrace();}
        System.out.println("==========sendEmail");
    }
    public static synchronized void sendMessage(){
        System.out.println("======sendMessage");
    }
    public void hello(){
        System.out.println("sayHello");
    }
}
