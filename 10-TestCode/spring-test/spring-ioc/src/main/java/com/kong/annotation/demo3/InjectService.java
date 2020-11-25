package com.kong.annotation.demo3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InjectService {
    @Autowired
    @Qualifier("tag1")
    private Map<String, IService> serviceMap1;

    @Autowired
    @Qualifier("tag2")
    private Map<String, IService> serviceMap2;

    @Override
    public String toString() {
        return "InjectService{" +
                "serviceMap1=" + serviceMap1 +
                ", serviceMap2=" + serviceMap2 +
                '}';
    }
}
