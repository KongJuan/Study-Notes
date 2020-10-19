package com.kong.aop.test;
import com.kong.aop.service.UserService;


import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Test;
import org.springframework.aop.*;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.w3c.dom.ls.LSOutput;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Method;

public class AopTest01 {
    @Test
    public void test1(){
        /**
         * 需求：在work方法执行前，打印一句：你好：userName
         */
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

    @Test
    public void test2(){
        /**
         * 需求：统计一下work方法的耗时，将耗时输出
         */
        //定义目标对象
        UserService target=new UserService();
        //创建Pointcut，用来拦截UserService中的work方法
        Pointcut pointcut=new Pointcut() {
            @Override
            public ClassFilter getClassFilter() {
                //判断是否又UserService类型的
                return clazz->UserService.class.isAssignableFrom(clazz);
            }

            @Override
            public MethodMatcher getMethodMatcher() {

                return new MethodMatcher() {
                    @Override
                    public boolean matches(Method method, Class<?> aClass) {
                        //判断方法名是否是work方法
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
        //创建通知，需要拦截方法的执行，所以用到MethodInterceptor类型的通知
        MethodInterceptor advice=new MethodInterceptor() {
            @Override
            public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                System.out.println("准备调用："+methodInvocation.getMethod());
                long starTime= System.nanoTime();
                //执行方法
                Object result=methodInvocation.proceed();
                long endTime= System.nanoTime();
                System.out.println("调用结束！耗时(纳秒):" + (endTime - starTime));
                return null;
            }
        };
        //创建Advisor,将pointcut和advice组装起来
        DefaultPointcutAdvisor advisor=new DefaultPointcutAdvisor(pointcut,advice);
        //通过spring提供的代理创建工厂来创建代理
        ProxyFactory proxyFactory=new ProxyFactory();
        //为工厂指定目标对象
        proxyFactory.setTarget(target);
        //调用addAdvisor方法，为目标添加增强的功能，即添加Advisor，
        // 可以为目标添加很多个Advisor
        proxyFactory.addAdvisor(advisor);
        //通过工厂提供的方法来生成代理对象
        UserService userServiceProxy = (UserService) proxyFactory.getProxy();
        //调用代理的work方法
        userServiceProxy.work(" lala");
    }

    @Test
    public void test3(){
        /**
         * userName中包含“粉丝”关键字，输出一句：感谢您一路的支持
         * 此处需要用到 MethodMatcher 中的动态匹配了，通过参数来进行判断。
         * 重点在于Pointcut中的getMethodMatcher方法，返回的MethodMatcher， isRuntime必须返回true，
         * 此时才会进入到matches中对参数进行校验。
         */
        //定义目标对象
        UserService target=new UserService();
        //创建pointcut，用来拦截UserService中的work方法
        Pointcut pointcut=new Pointcut() {
            @Override
            public ClassFilter getClassFilter() {
                //判断是否是UserService类型的
                return clazz->UserService.class.isAssignableFrom(clazz);
            }

            @Override
            public MethodMatcher getMethodMatcher() {
                return new MethodMatcher() {
                    @Override
                    public boolean matches(Method method, Class<?> aClass) {
                        //判断方法名是否是work
                        return "work".equals(method.getName());
                    }

                    @Override
                    public boolean isRuntime() {
                        return true; //这个地方需返回true,才能执行matches方法
                    }

                    @Override
                    public boolean matches(Method method, Class<?> aClass, Object[] objects) {
                        //isRuntime为true的时候，会执行这个方法
                        if(objects!=null && objects.length==1){
                            String userName=(String)objects[0];
                            return userName.contains("粉丝");
                        }
                        return false;
                    }
                };
            }
        };
        //创建通知，此处需要在方法之前执行操作，所以需要用到MethodBeforeAdvice类型的通知
        MethodBeforeAdvice advice = (method, args, target1) -> System.out.println("感谢您一路的支持!");
        //创建Advisor，将pointcut和advice组装起来
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, advice);
        //通过spring提供的代理创建工厂来创建代理
        ProxyFactory proxyFactory = new ProxyFactory();
        //为工厂指定目标对象
        proxyFactory.setTarget(target);
        //调用addAdvisor方法，为目标添加增强的功能，即添加Advisor，
        // 可以为目标添加很多个Advisor
        proxyFactory.addAdvisor(advisor);
        //通过工厂提供的方法来生成代理对象
        UserService userServiceProxy = (UserService) proxyFactory.getProxy();
        //调用代理的work方法
        userServiceProxy.work("粉丝：A");
    }
}
