package com.kyn;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringCloudApplication
@EnableFeignClients("com.kyn.service")
public class ConsumerApplication8090 {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication8090.class, args);
    }

}
