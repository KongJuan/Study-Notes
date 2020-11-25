package com.kong.annotation.demo1;

import org.springframework.context.annotation.Bean;

public class ConfigBean {

    //bean名称为方法默认值：user1
    @Bean
    public User user1(){
        return new User();
    }

    //bean名称通过value指定了：user2Bean
    @Bean("user2Bean")
    public User user2(){
        return new User();
    }

    //bean名称为：user3Bean，2个别名：[user3BeanAlias1,user3BeanAlias2]
    @Bean({"user3Bean", "user3BeanAlias1", "user3BeanAlias2"})
    public User user3(){
        return new User();
    }

}
