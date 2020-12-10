package com.kyn.juctest01;

import org.omg.CORBA.TIMEOUT;

import java.util.concurrent.TimeUnit;

public class VolatileDemo03 {
    volatile int a=0;

    volatile boolean flag=false;

/*

多线程下flag=true可能先执⾏，还没⾛到a=1就被挂起。

其它线程进⼊method02的判断，修改a的值=5，⽽不是6。

*/

    public void method01(){
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        a=1;

        flag=true;
    }

    public void method02(){

        if (flag){

            a+=5;

            System.out.println("*****最终值a: "+a);
        }
    }

    public static void main(String[] args) {

        VolatileDemo03 resortSeq = new VolatileDemo03();

        new Thread(()->{resortSeq.method01();},"ThreadA").start();

        new Thread(()->{resortSeq.method02();},"ThreadB").start();
    }

    }
