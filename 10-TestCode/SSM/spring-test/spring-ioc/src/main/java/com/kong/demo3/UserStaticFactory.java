package com.kong.demo3;

public class UserStaticFactory {
    /**
     * 静态无参方法创建UserModel
     *
     * @return
     */
    public static UserModel buildUser1() {
        System.out.println(UserStaticFactory.class + " .buildUser1()");
        UserModel userModel = new UserModel();
        return userModel;
    }

    /**
     * 静态有参方法创建UserModel
     *
     * @param name 名称
     * @param age 年龄
     * @return
     */
    public static UserModel buildUser2(String name, int age) {
        System.out.println(UserStaticFactory.class + ".buildUser2()");
        UserModel userModel = new UserModel(name,age);
        return userModel;
    }


}
