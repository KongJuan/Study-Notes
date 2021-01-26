package com.kyn.controller;

import com.kyn.pojo.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }

    @PostMapping("/login")
    public String main(User user, HttpSession session, Model model){
        if(StringUtils.hasLength(user.getName())){
            session.setAttribute("loginUser",user);
            return "main";
        }
        model.addAttribute("msg","用户名错误");
        return "login";
    }
}
