package com.kyn.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyn.mapper.UserMapper;
import com.kyn.pojo.User;
import com.kyn.service.UserService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("getPageUser")
    public String getPageUser(@RequestParam(value = "pn",defaultValue = "1") Integer pn,Model model){

        /**
         * pn:表示第几页
         * 第二个参数是每页显示几行
         */
        Page<User> page=new Page<>(pn,2);
        Page<User> userPage = userService.page(page, null);
        model.addAttribute("users",userPage);
        return "test";
    }

    @GetMapping("deleteUser/{id}")
    public String deleteUser(@PathVariable("id") int id,
                             @RequestParam(value="pn",defaultValue = "1") int pn,
                             RedirectAttributes ra){
        userService.removeById(id);
        //重定向时携带参数
        ra.addAttribute("pn",pn);
        return "redirect:/getPageUser";
    }

}
