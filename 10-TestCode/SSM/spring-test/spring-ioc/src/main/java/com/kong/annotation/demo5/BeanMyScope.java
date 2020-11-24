package com.kong.annotation.demo5;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class BeanMyScope implements Scope {

    //定义了一个常量，作为作用域的值
    public static final String SCOPE_MY="my";

    //自定义作用域会自动调用这个get方法来创建bean对象
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        System.out.println("BeanMyScope >>>>>>>>> get:" + name);
        //通过objectFactory.getObject()获取bean实例返回。
        return objectFactory.getObject();
    }

    @Override
    public Object remove(String s) {
        return null;
    }

    @Override
    public void registerDestructionCallback(String s, Runnable runnable) {

    }

    @Override
    public Object resolveContextualObject(String s) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }
}
