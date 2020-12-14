package com.kyn.jucLockTest;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SaleTicketDemo {
    public static void main(String[] args) {

        Ticket ticket = new Ticket();
        new Thread(()->{ for (int i = 1; i <= 30 ; i++)
            ticket.saleTicket(); }, "A").start();
        new Thread(()->{ for (int i = 1; i <= 30 ; i++)
            ticket.saleTicket(); }, "B").start();
        new Thread(()->{ for (int i = 1; i <= 30 ; i++)
            ticket.saleTicket(); }, "C").start();
    }
}
class Ticket{
    private int number = 30;
    Lock lock = new ReentrantLock(true);
    public void saleTicket(){
        lock.lock();
        try {
            if (number > 0) {
                System.out.println(Thread.currentThread().getName()+"\t卖出第："+(number--)+"\t还剩下："+number);
            }
        }catch(Exception e){
                e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }
}