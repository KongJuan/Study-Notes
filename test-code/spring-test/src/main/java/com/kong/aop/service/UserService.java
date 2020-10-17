package com.kong.aop.service;

//案例一
public class UserService {
    /**
     * 在work方法执行前，对work方法进行增强
     * 需求：在work方法执行前，打印一句：你好：userName
     * @param userName
     */
    public void work(String userName){
        System.out.println(userName+",正在吃饭");
    }
}
