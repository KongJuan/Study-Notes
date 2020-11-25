package com.kong.demo3;

/**
 * 器创建bean实例的方式
 * 1.通过分反射调用构造方法创建bean对象
 * 2.通过静态工厂方法创建bean对象
 * 3.通过实例工厂方法创建bean对象
 * 4.通过FactoryBean创建bean对象
 */
public class UserModel {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public UserModel() {
       this.name="我是通过无参构造方法创建的";
    }

    public UserModel(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
