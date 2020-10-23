package com.kong.test;

import com.kong.demo1.HelloWorld;
import com.kong.demo2.UserModel;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class IocTest01 {
    /**
     * Spring容器使用步骤
     * 1. 引入spring相关的maven配置
     * 2. 创建bean配置文件，比如bean xml配置文件
     * 3. 在bean xml文件中定义好需要spring容器管理的bean对象
     * 4. 创建spring容器，并给容器指定需要装载的bean配置文件，当spring容器启动之后，会加载这些
     * 配置文件，然后创建好配置文件中定义好的bean对象，将这些对象放在容器中以供使用
     * 5. 通过容器提供的方法获取容器中的对象，然后使用
     */
    @Test
    public void test01(){
        //1.创建ClassPathXmlApplicationContext容器，给容器指定需要加载的bean配置文件
        ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("beans1.xml");
        //2.从容器中获取需要的bean
        HelloWorld helloWorld=context.getBean(HelloWorld.class);
        //3.使用对象
        helloWorld.say();
    }

    @Test
    public void test02(){
        ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext("beans2.xml");
        //getBeanDefinitionNames用于获取容器中所有bean的名称
        for(String beanName:context.getBeanDefinitionNames()){
            //getAliases:通过bean名称获取这个bean的所有别名
            String[] aliases=context.getAliases(beanName);
            System.out.println(String.format("beanName:%s,别名:[%s]", beanName, String.join(",", aliases)));
        }
    }

}
