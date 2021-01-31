package com.kyn.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

//@Configuration
public class DepartCodeConfig {
    //@LoadBalanced   //开启消费者客户端的负载均衡功能
    //@Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
