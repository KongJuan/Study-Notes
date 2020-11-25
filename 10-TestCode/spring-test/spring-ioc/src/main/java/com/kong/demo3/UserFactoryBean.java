package com.kong.demo3;

import org.springframework.beans.factory.FactoryBean;

public class UserFactoryBean implements FactoryBean<UserModel> {
    @Override
    public UserModel getObject() throws Exception {
        UserModel userModel = new UserModel();
        return userModel;
    }

    @Override
    public Class<?> getObjectType() {
        return UserModel.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
