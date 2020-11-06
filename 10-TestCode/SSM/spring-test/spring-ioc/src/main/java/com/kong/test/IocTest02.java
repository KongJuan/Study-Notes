package com.kong.test;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class IocTest02 {
    ClassPathXmlApplicationContext context;
    @Before
    public void before(){
        context=new ClassPathXmlApplicationContext("beans4.xml");
        System.out.println("spring容器启动完毕！");
    }
    @Test
    public void test01(){
        System.out.println("--------单例bean，每次获取的bean实例都一样----------");
        System.out.println(context.getBean("singletonBean"));
        System.out.println(context.getBean("singletonBean"));
        System.out.println(context.getBean("singletonBean"));
    }

    @Test
    public void test02(){
        System.out.println("---------多例bean，每次获取都会重新创建一个bean实例对象---------");
        System.out.println(context.getBean("prototypeBean"));
        System.out.println(context.getBean("prototypeBean"));
        System.out.println(context.getBean("prototypeBean"));
    }

}
