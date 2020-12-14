package com.kyn.jucCasTest;

import java.util.concurrent.atomic.AtomicReference;

public class CASTest01 {
    public static void main(String[] args) {
        User user1=new User("张三",22);
        User user2=new User("里斯",23);

        AtomicReference<User> atomicReference=new AtomicReference<>();

        atomicReference.set(user1);

        System.out.println(atomicReference.compareAndSet(user1,user2)+"\t"+atomicReference.get());
        System.out.println(atomicReference.compareAndSet(user1,user2)+"\t"+atomicReference.get());
    }
}
