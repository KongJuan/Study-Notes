package com.kyn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ProviderApplication8084 {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication8084.class, args);
    }

}
