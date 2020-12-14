package com.kj.controller;

import com.kj.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("user")
@Controller
public class UserController {
    @RequestMapping("findUserById")
    public String findUserById(Integer id, Model model, HttpServletRequest request){
        model.addAttribute("msg", "直接参数绑定接收到的参数："+id);
        //model.addAttribute("msg", "通过Request getParameter参数接收到的参 数："+request.getParameter("id"));
        return "success";
    }

    @RequestMapping("findUserById2")
    public String findUserById2(@RequestParam("uid") Integer id, Model model) {
        model.addAttribute("msg", "接收到的参数："+id);
        return "success";
    }

    @RequestMapping("saveUser")
    public String saveUser(User user, Model model)
    {
        model.addAttribute("msg", "接收到的参数："+user.toString());
        return "success";
    }
}
