package com.kyn.controller;

import com.kyn.pojo.Depart;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/consumer/depart")
public class DepartController {

    @Autowired
    private RestTemplate restTemplate;
    //private static final String SERVER_PROVIDER="http://localhost:8081";
    //将原生的“主机名+端口号”修改为提供者的微服务名称
    private static final String SERVER_PROVIDER="http://kyn01-provider-depart";

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

    //指定该方法要按使用服务降级，即若当前的方法在运行过程中发生异常，
    //无法给客户端正常响应时，就会调用fallbackMethod指定的方法
    @HystrixCommand(fallbackMethod = "getHystrixHandle")
    @GetMapping("/get/{id}")
    public Depart getDepartHandle(@PathVariable("id") int id){
        String url=SERVER_PROVIDER+"/provider/depart/get/"+id;
        return restTemplate.getForObject(url,Depart.class);
    }

    public Depart getHystrixHandle(@PathVariable("id") int id){
        Depart depart=new Depart();
        depart.setId(id);
        depart.setDepName("no this depart");
        return depart;
    }

    @GetMapping("/list")
    public List<Depart> listAllDepartsHandle(){
        String url=SERVER_PROVIDER+"/provider/depart/list/";
        return restTemplate.getForObject(url,List.class);
    }
}
