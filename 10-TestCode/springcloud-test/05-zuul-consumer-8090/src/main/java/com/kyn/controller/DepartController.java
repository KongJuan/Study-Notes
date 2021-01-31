package com.kyn.controller;

import com.kyn.pojo.Depart;
import com.kyn.service.DepartService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/consumer/depart")
public class DepartController {

    @Autowired
    private DepartService departService;

    @PostMapping("/save")
    public boolean saveHandle(@RequestBody Depart depart){
        return departService.saveDepart(depart);
    }

    @DeleteMapping("del/{id}")
    public boolean delHandle(@PathVariable("id") int id){
        return departService.removeDepartById(id);
    }

    @PutMapping("/update")
    public boolean updateHandle(@RequestBody Depart depart){
        return departService.modifyDepart(depart);
    }

    @HystrixCommand(fallbackMethod = "getHystrixHandle")
    @GetMapping("/get/{id}")
    public Depart getDepartHandle(@PathVariable("id") int id){
        return departService.getDepartById(id);
    }

    public Depart getHystrixHandle(@PathVariable("id") int id){
        Depart depart=new Depart();
        depart.setId(id);
        depart.setDepName("no this depart-8090");
        return depart;
    }

    @GetMapping("/list")
    public List<Depart> listAllDepartsHandle(){
        return departService.listAllDeparts();
    }
}
