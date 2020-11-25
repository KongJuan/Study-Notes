package com.kong.annotation.demo2;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class InjectTest {
    @Test
    public void test0() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MainConfig0.class);
        for (String beanName : context.getBeanDefinitionNames()) {
            System.out.println(String.format("%s->%s", beanName, context.getBean(beanName)));
        }
    }
}
