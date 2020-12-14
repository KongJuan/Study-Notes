package com.kj.example.JUC01;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class CollectionSafeTest {
    public static void main(String[] args) {
        listNoteSafe();
    }
    private static void listNoteSafe(){
        //List<String> list=new ArrayList<String>();
        List<String> list=new CopyOnWriteArrayList<>();
        for(int i=1;i<=10;i++){
            new Thread(()->{
                list.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(Thread.currentThread().getName()+"/t"+list);
            },String.valueOf(i)).start();
        }
    }
}
