package com.kyn.controller;

import com.kyn.pojo.Pet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    Pet pet;

    @RequestMapping("/test")
    public String test(){
        return "Hello SpringBoot";
    }

    @RequestMapping("/pet")
    public Pet pet(){
        return pet;
    }
}
