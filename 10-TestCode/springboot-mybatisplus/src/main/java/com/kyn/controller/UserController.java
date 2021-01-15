package com.kyn.controller;

import com.kyn.mapper.UserMapper;
import com.kyn.pojo.User;
import com.kyn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("getAllUser")
    public String getAllUser(Model model){
        model.addAttribute("users",userService.list());
        return "test";
    }
}
