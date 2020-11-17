package com.kong.annotation.demo2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Service5 {
    /*
    //6.@Autowire标注字段，多个候选者的时候，按字段名称注入
    @Autowired
    private IService service3;
    @Override
    public String toString() {
        return "Service5{" +
                "service3=" + service3 +
                '}';
    }
    */

    //7.将指定类型的所有bean，注入到Collection、Map中
    @Autowired
    private List<IService> services;

    @Autowired
    private Map<String,IService> serviceMap;

    @Override
    public String toString() {
        return "Service5{" +
                "services=" + services +
                ", serviceMap=" + serviceMap +
                '}';
    }
}
