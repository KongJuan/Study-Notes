package com.kong.aop.test;
import com.kong.aop.service.UserService;


import org.aopalliance.aop.Advice;
import org.junit.Test;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;

import java.lang.reflect.Method;

public class AopTest01 {
    @Test
    public void test1(){
        //定义目标对象
        UserService target=new UserService();
        //创建pointcut,用来拦截UserService中的work方法
        Pointcut pointcut=new Pointcut(){

            public ClassFilter getClassFilter() {
                //判断是否是UserService类型的
                // class1.isAssignableFrom(class2) 判定此 Class 对象所表示的类或接口与指定的 Class 参数所表示的类或接口是否相同，
                // 或是否是其超类或超接口。如果是则返回 true；否则返回 false。
                // 如果该 Class 表示一个基本类型，且指定的 Class 参数正是该 Class 对象，
                // 则该方法返回 true；否则返回 false。
                return clazz->UserService.class.isAssignableFrom(clazz);
            }

            public MethodMatcher getMethodMatcher() {
                return new MethodMatcher() {
                    @Override
                    public boolean matches(Method method, Class<?> aClass) {
                        //判断方法名是否是work
                        return "work".equals(method.getName());
                    }

                    @Override
                    public boolean isRuntime() {
                        return false;
                    }

                    @Override
                    public boolean matches(Method method, Class<?> aClass, Object[] objects) {
                        return false;
                    }
                };
            }
        };
        //创建通知，此处需要在方法之前执行操作，所以需要用到MethodBeforeAdvice类型的通知
        MethodBeforeAdvice advice=(method,args,target1)-> System.out.println("您好："+args[0]);
        //创建Advisor,将pointcut和advice组装起来
        DefaultPointcutAdvisor advisor=new DefaultPointcutAdvisor(pointcut,advice);
        //通过spring提供的代理创建工厂来创建代理
        ProxyFactory proxyFactory=new ProxyFactory();
        //为工厂指定目标对象
        proxyFactory.setTarget(target);
        //调用addAdvisor方法，为目标添加增强的功能，即添加Advisor,可以为目标添加很多个Advisor
        proxyFactory.addAdvisor(advisor);
        //通过工厂提供的方法来生成代理对象
        UserService userServiceProxy=(UserService)proxyFactory.getProxy();
        //调用代理对象的work方法
        userServiceProxy.work("啦啦啦");

    }
}
