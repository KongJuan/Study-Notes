package com.kyn.controller;

import com.kyn.pojo.Depart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

    @DeleteMapping("del/{id}")
    public void delHandle(@PathVariable("id") int id){
        String url=SERVER_PROVIDER+"/provider/depart/del/"+id;
        restTemplate.delete(url);
    }

    @PutMapping("/update")
    public void updateHandle(@RequestBody Depart depart){
        String url=SERVER_PROVIDER+"/provider/depart/update";
        restTemplate.put(url,depart);
    }

    @GetMapping("/get/{id}")
    public Depart getDepartHandle(@PathVariable("id") int id){
        String url=SERVER_PROVIDER+"/provider/depart/get/"+id;
        return restTemplate.getForObject(url,Depart.class);
    }

    @GetMapping("/list")
    public List<Depart> listAllDepartsHandle(){
        String url=SERVER_PROVIDER+"/provider/depart/list/";
        return restTemplate.getForObject(url,List.class);
    }
}
