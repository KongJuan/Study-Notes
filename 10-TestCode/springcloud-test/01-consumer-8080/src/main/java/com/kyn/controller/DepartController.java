package com.kyn.controller;

import com.kyn.pojo.Depart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/consumer/depart")
public class DepartController {

    @Autowired
    private RestTemplate restTemplate;
    private static final String SERVER_PROVIDER="http://localhost:8081";

    @PostMapping("/save")
    public boolean saveHandle(@RequestBody Depart depart){
        String url=SERVER_PROVIDER+"/provider/depart/save";
        return restTemplate.postForObject(url,depart,Boolean.class);
    }

}
