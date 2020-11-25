package com.kong.annotation.demo2;

import com.kong.annotation.demo1.ServiceA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Service2 {


    /*public Service2(){
        System.out.println(this.getClass()+"无参构造器");
    }

    //1.通过构造器注入依赖对象
    private Service1 service1;
    @Autowired
    public Service2(Service1 service1){
        System.out.println(this.getClass()+"有参构造器");
        this.service1=service1;
    }*/

    /*
    //2.通过方法注入依赖的对象
     private Service1 service1;
    @Autowired
    public void injectService1(Service1 service1){
        System.out.println(this.getClass().getName()+"injectService1(Service1 service1)");
        this.service1=service1;
    }*/

    /*//3.通过setter方法注入
    private Service1 service1;
    @Autowired
    public void setService1(Service1 service1) {
        System.out.println(this.getClass().getName() + ".setService1方法");
        this.service1 = service1;
    }*/

    /*//4.方法中有多个参数，方法上面的@Autowire默认对方法中所有参数起效，如果在spring容器中找不到对应的组件会报异常。
    // 如果我们想对某个参数进行特定的配置，可以在参数上加上@Autowired，这个配置会覆盖方法上面的@Autowired配置。
    private Service1 service1;
    @Autowired
    public void injectService1(Service1 service1, String name)
    {
        System.out.println(String.format("%s.injectService1(), {service1=%s,name=%s}", this.getClass().getName(), service1, name));
        this.service1 = service1;
    } //异常：NoSuchBeanDefinitionException

    //在第二个参数上面加上@Autowired，设置required为false：表示这个bean不是强制注入的，
    // 能找到就注入，找不到就注入一个null对象
    @Autowired
    public void injectService2(Service1 service1, @Autowired(required = false)String name)
    {
        System.out.println(String.format("%s.injectService1(), {service1=%s,name=%s}", this.getClass().getName(), service1, name));
        this.service1 = service1;
    }//NoSuchBeanDefinitionException*/


    //5.@Autowired用在字段上
    @Autowired
    private Service1 service1;

    @Override
    public String toString() {
        return "Service2{" +
                "service1=" + service1 +
                '}';
    }
}
