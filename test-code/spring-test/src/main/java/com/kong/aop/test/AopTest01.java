package com.kong.aop.test;
import com.kong.aop.service.UserService;


import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

public class AopTest01 {
    @Test
    public void test1(){
        //定义目标对象
        UserService target=new UserService();
        //创建pointcut,用来拦截UserService中的work方法
        Pointcut pointcut=new Pointcut(){

            public ClassFilter getClassFilter() {
                return null;
            }

            public MethodMatcher getMethodMatcher() {
                return null;
            }
        };

    }
}
