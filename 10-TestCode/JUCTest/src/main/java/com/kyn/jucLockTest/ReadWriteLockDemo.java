package com.kyn.jucLockTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockDemo {
    public static void main(String[] args) {
        MyCache myCache=new MyCache();

        //写
        for (int i = 1; i <=10; i++) {
            final int finalI = i;
            new Thread(()->{
                myCache.put(String.valueOf(finalI),String.valueOf(finalI));
            },String.valueOf(i)).start();
        }

        //读
        for (int i = 1; i <=5; i++) {
            final int finalI = i;
            new Thread(()->{
                myCache.get(String.valueOf(finalI));
            },String.valueOf(i)).start();
        }
    }
}
class MyCache{
    private Map<String,Object> map=new HashMap<>();
    private ReentrantReadWriteLock rw=new ReentrantReadWriteLock();

    //写锁
    public void put(String key,Object value){
        rw.writeLock().lock();
        try{
            System.out.println(Thread.currentThread().getName()+"\t正在写入");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            map.put(key,value);
            System.out.println(Thread.currentThread().getName()+"\t写入成功");
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            rw.writeLock().unlock();
        }
    }

    public Object get(String key){
        Object result=null;
        rw.readLock().lock();
        try{
            System.out.println(Thread.currentThread().getName() + "\t正在读取:" + key);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            result=map.get(key);
            System.out.println(Thread.currentThread().getName()+"\t读取完成： "+result);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            rw.readLock().unlock();
        }
        return result;
    }
}
