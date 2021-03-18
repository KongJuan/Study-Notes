package com.kyn.blockingqueue;

import java.sql.Time;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MyResource {
    private volatile boolean FLAG=true; //默认开启，进行生产和消费
    private AtomicInteger atomicInteger=new AtomicInteger();

    private BlockingQueue<String> blockingQueue;

    public MyResource(BlockingQueue<String> blockingQueue){
        this.blockingQueue=blockingQueue;
        System.out.println(blockingQueue.getClass().getName());
    }
    public void myProd() throws InterruptedException {
        String data=null;
        boolean retValue;
        while(FLAG){
            data=atomicInteger.incrementAndGet()+"";
            retValue=blockingQueue.offer(data,2L, TimeUnit.SECONDS);
            if(retValue){
                System.out.println(Thread.currentThread().getName() + "\t"
                        + "插⼊队列" + data + "成功");
            } else {
                System.out.println(Thread.currentThread().getName() + "\t"
                        + "插⼊队列" + data + "失败");
            }
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(Thread.currentThread().getName() + "\t⽼板叫停了，FLAG已更新为false，停⽌⽣产");
    }
    public void myCons() throws InterruptedException {
        String res;
        while(FLAG){
            res=blockingQueue.poll(2L,TimeUnit.SECONDS);
            if(null==res || "".equals(res)){
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
