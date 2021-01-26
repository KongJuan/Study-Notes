package com.kyn.controller;

import com.kyn.mapper.UserMapper;
import com.kyn.pojo.User;
import com.kyn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @RequestMapping("/add")
    public String addUser(){
        User user=new User("李四",18);
        int res=userService.addUser(user);
        if(res>0) {
            return "添加成功";
        }
        return "添加失败";
    }

    @RequestMapping("getUser")
    public User getUserByName(){
        return userService.getUserByName("张三");
    }

    @RequestMapping("/getAllUser")
    public List<User> getAllUser(){
        return userService.getAllUser();
    }

    @GetMapping("/user")
    public User getUser(@RequestParam("name") String name){
        return userService.getUser(name);
    }


}
