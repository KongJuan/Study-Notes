package com.kyn.jucThreadPoolTest;

import java.util.concurrent.*;

public class ThreadPoolDemo {
    public static void main(String[] args) {
        /*System.out.println("=======Fixed Thread Pool========");
        //⼀个池⼦有5个⼯作线程，类似银⾏有5个受理窗⼝,等待队列为Integer.MAX_VALUE
        threadPoolExecutor(Executors.newFixedThreadPool(5));*/

        /*System.out.println("======Single Thread Pool=========");
        //⼀个池⼦有1个⼯作线程，类似银⾏有1个受理窗⼝,等待队列为Integer.MAX_VALUE
        threadPoolExecutor(Executors.newSingleThreadExecutor());*/

        /*System.out.println("=====Cached Thread Pool=======");
        //不定量线程，⼀个池⼦有N个⼯作线程，类似银⾏有N个受理窗⼝
        threadPoolExecutor(Executors.newCachedThreadPool());*/

        System.out.println("=====Custom Thread Pool=======");
        //自定义线程池和等待队列数量
        threadPoolExecutor(new ThreadPoolExecutor(2,
                5,
                1L,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy()));
    }
    private static void threadPoolExecutor(ExecutorService executor){
        try{
            for (int i = 1; i <=10 ; i++) {
                executor.execute(()->{
                    System.out.println(Thread.currentThread().getName()+"\t正在办理业务");
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            executor.shutdown();
        }
    }
}
