package com.kong.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;

@Controller
public class HelloController {

    @RequestMapping("/showData")
    @ResponseBody
    public String showData(){
        return "showData";
    }

    @RequestMapping("/modelandview")
    public ModelAndView result(){
        ModelAndView mv=new ModelAndView();
        mv.addObject("time",new Date());
        mv.getModel().put("name","haha");
        return mv;
    }
}
