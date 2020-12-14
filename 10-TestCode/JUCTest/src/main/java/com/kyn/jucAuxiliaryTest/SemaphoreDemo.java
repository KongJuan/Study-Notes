package com.kyn.jucAuxiliaryTest;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Semaphore： 信号灯。 适用场景：限制资源，如抢位置、限流等。
 * CountDownLatch 的问题是不能复⽤，⽐如 count=3 ，那么加到3，就不能继续操作了。
 * ⽽ Semaphore 可以解决这个问题
 *          ⽐如6辆⻋3个停⻋位，对于 CountDownLatch 只能停3辆⻋，
 *          ⽽ Semaphore 可以停6辆⻋，⻋位空出来后，其它⻋可以占有。
 * Semaphore编码模型：
 *         1、创建信号灯
 *         Semaphore semaphore = new Semaphore(3); // 3个位置
 *         2、等待获取信号灯
 *          semaphore.acquire();//等待获取许可证
 *         3、业务代码
 *         4、释放信号
 *         semaphore.release();//释放资源,车离开了，就要释放车位
 */
public class SemaphoreDemo {
    public static void main(String[] args) {
        Semaphore semaphore=new Semaphore(3);
        for (int i = 1; i <=6 ; i++) {
            new Thread(()->{
                try{
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName()+"\t抢到⻋位");
                    try { TimeUnit.SECONDS.sleep(3); } catch
                    (InterruptedException e) {e.printStackTrace(); }

                    System.out.println(Thread.currentThread().getName()+"\t停⻋3秒后离开⻋位");
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }
            }).start();
        }
    }
}
