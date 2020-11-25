package com.kong.test;

import com.kong.annotation.demo1.ConfigBean2;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;

public class AnnoTest01 {
    @Test
    public void test01(){
        AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(ConfigBean2.class);
        for(String beanName:context.getBeanDefinitionNames()){
            String[] aliases=context.getAliases(beanName);
            System.out.println(String.format("bean名称:%s,别名:%s,bean对象:%s",
                    beanName,
                    Arrays.asList(aliases),
                    context.getBean(beanName)));
        }
    }
}
