package com.kyn.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
* 使用阻塞队列实现生产者与消费者的好处是我们不需要关⼼什么时候需要阻塞线程，什么时候需要唤醒线程，因为这⼀切BlockingQueue都
 * 给你⼀⼿包办好了，使⽤阻塞队列 后就不需要⼿动加锁了。
*/
public class ProdConsBlockQueueDemo {
    public static void main(String[] args) {
        MyResource myResource = new MyResource(new ArrayBlockingQueue<>(5));
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t⽣产线程启动");
            try {
                myResource.myProd();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "prod").start();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t⽣产线程启动");
            try {
                myResource.myProd();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "prod-2").start();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t消费线程启动");
            try {
                myResource.myCons();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "cons").start();
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t消费线程启动");
            try {
                myResource.myCons();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "cons-2").start();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("5秒钟后，叫停");
        myResource.stop();
    }
}
class MyResource{
    private volatile boolean FLAG=true; //默认开启，进行生产和消费
    private AtomicInteger atomicInteger=new AtomicInteger();
    private BlockingQueue<String> blockingQueue;

    public MyResource(BlockingQueue<String> blockingQueue) {

        
        this.blockingQueue = blockingQueue;
        System.out.println(blockingQueue.getClass().getName());

    }

    //生产者
    public void myProd() throws InterruptedException {
        String data=null;
        boolean retValue;
        while(FLAG){
            data=atomicInteger.incrementAndGet()+"";
            retValue=blockingQueue.offer(data,2L, TimeUnit.SECONDS);
            if(retValue){
                System.out.println(Thread.currentThread().getName() + "\t"+ "插⼊队列" + data + "成功");
            } else {
                System.out.println(Thread.currentThread().getName() + "\t" + "插⼊队列" + data + "失败");
            }
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(Thread.currentThread().getName() + "\t⽼板叫停了，FLAG已更新为false，停⽌⽣产");
    }
    public void myCons() throws Exception {
        String res;
        while (FLAG) {
            res = blockingQueue.poll(2L, TimeUnit.SECONDS);
            if (null == res || "".equals(res)) {
                // FLAG = false;
                System.out.println(Thread.currentThread().getName() + "\t超过2秒钟没有消费，退出消费");
                return;
            }
            System.out.println(Thread.currentThread().getName() + "\t\t消费队列" + res + "成功");
        }
    }

    public void stop() {
        this.FLAG = false;
    }

}
