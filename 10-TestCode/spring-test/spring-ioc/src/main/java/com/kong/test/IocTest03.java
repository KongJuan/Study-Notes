package com.kong.test;

import com.kong.demo5.NormalBean;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class IocTest03 {
    @Test
    public void test01(){
        ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("beans5.xml");
        NormalBean.IService service=context.getBean(NormalBean.IService.class);
        System.out.println(service);
    }

}
