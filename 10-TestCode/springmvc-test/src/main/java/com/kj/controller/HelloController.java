package com.kj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloController {
    /**
     * 接收请求
     * @return
     */
    @RequestMapping("/hello")
    public String sayHello() {
        System.out.println("Hello SpringMVC!!");
        return "success";
    }
}
