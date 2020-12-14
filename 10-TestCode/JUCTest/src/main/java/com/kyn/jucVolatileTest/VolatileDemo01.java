package com.kyn.jucVolatileTest;

import java.util.concurrent.TimeUnit;

public class  VolatileDemo01{
    public static void main(String[] args) {
        volatileVisibilityDemo();
    }

    //Volatile可以保证可见性，当主内存的值被修改，能够及时通知到其他线程
    private static void volatileVisibilityDemo(){
        System.out.println("可见性测试");
        MyData01 data=new MyData01();

        //启动一个线程
        new Thread(()->{
            System.out.println(Thread.currentThread().getName() + "\t 执⾏");
            try {
                TimeUnit.SECONDS.sleep(3);
                data.setNum();
                System.out.println(Thread.currentThread().getName()+"\t更新number值: " + data.num);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },"ThreadA").start();
        while(data.num==0){
            //main线程持有共享数据的拷⻉，⼀直为0
        }
        System.out.println(Thread.currentThread().getName() + "\t main获取number值: " + data.num);

    }
}

class MyData01 {
    //int num=0;
    volatile int num=0;

    public void setNum() {
        this.num = 60;
    }
}

/**
 * 执行结果：
 * 1、num不加volatile,main线程不知道num被修改了
 * 可见性测试
 * ThreadA	 执⾏
 * ThreadA	更新number值: 60
 * 2、num加volatile
 * 可见性测试
 * ThreadA	 执⾏
 * ThreadA	更新number值: 60
 * main	 main获取number值: 60
 */

