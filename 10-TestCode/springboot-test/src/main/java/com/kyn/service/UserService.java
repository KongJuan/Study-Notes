package com.kyn.service;

import com.kyn.dao.UserDao;
import com.kyn.mapper.UserMapper;
import com.kyn.pojo.User;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    UserMapper userMapper;

    public int addUser(User user){
        return userDao.addUser(user);
    }

    public User getUserByName(String name){
        return userDao.getUserByName(name);
    }

    public List<User> getAllUser(){
        return userDao.getAllBooks();
    }

    public User getUser(String name){
        return userMapper.getUser(name);
    }

}
