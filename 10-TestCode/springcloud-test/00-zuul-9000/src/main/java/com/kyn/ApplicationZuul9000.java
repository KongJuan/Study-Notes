package com.kyn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy   //开启zuul的代理模式
@SpringBootApplication
public class ApplicationZuul9000 {

    public static void main(String[] args) {
        SpringApplication.run(ApplicationZuul9000.class, args);
    }

}
