package com.kyn.conf;

import com.kyn.pojo.Pet;
import com.kyn.pojo.User;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration  //等同于bean的xml配置文件
@EnableConfigurationProperties()
public class MyConfig {

    @ConditionalOnBean(name="pet")
    @Bean
    public User user(){
        return new User("zhangsan",18);
    }

    @Bean
    public Pet pet(){
        return new Pet("小黄","blue");
    }
}
