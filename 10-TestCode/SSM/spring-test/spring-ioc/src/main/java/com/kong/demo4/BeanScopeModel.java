package com.kong.demo4;

public class BeanScopeModel {
    public BeanScopeModel(String beanScope) {
        System.out.println(String.format("create BeanScopeModel,{sope=%s}, {this=%s}", beanScope, this));
    }
}
