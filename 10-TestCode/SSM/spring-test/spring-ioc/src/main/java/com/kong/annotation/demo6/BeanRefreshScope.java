package com.kong.annotation.demo6;


import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.concurrent.ConcurrentHashMap;

public class BeanRefreshScope implements Scope {

    public static final String SCOPE_REFRESH="refresh";

    private static final BeanRefreshScope INSTANCE=new BeanRefreshScope();

    //使用map来缓存bean
    private ConcurrentHashMap<String,Object> beanMap=new ConcurrentHashMap<>();

    private BeanRefreshScope() { }

    public static BeanRefreshScope getInstance() {
        return INSTANCE;
    }

    //用来清理beanMap中当前已缓存的所有bean
    public static void clean(){
        INSTANCE.beanMap.clear();
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        //从beanMap中获取，获取不到会调用objectFactory的getObject让spring创建
        //bean的实例，然后丢到beanMap中
        Object bean=beanMap.get(name);
        if(bean==null){
            bean=objectFactory.getObject();
            beanMap.put(name,bean);
        }
        return bean;
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
