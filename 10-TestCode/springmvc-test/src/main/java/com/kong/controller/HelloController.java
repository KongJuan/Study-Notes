package com.kong.controller;

import com.kong.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class HelloController {
    /**
     * 接收请求
     * @return
     */
    @RequestMapping(path="/hello")
    public String sayHello() {
        System.out.println("Hello SpringMVC!!");
        return "success";
    }
    @RequestMapping("/voidTest")
    public void voidTest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("voidTest!!");
        request.getRequestDispatcher("WEB-INF/pages/success.jsp").forward(request,response);
    }

    @RequestMapping("/modelandviewTest")
    public ModelAndView modelAndViewTest(){
        ModelAndView mv=new ModelAndView();
        //添加model数据到request
        mv.addObject("name","张三");
        //设置跳转页面
        mv.setViewName("success");
        return mv;
    }
    @RequestMapping("/paramsTest")
    public String paramsTest(String name){
        System.out.println(name);
        return "success";
    }
    @RequestMapping("/pojoTest")
    public String pojoTest(User user){
        System.out.println(user);
        return "success";
    }
}
