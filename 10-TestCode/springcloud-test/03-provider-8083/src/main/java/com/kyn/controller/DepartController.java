package com.kyn.controller;

import com.kyn.pojo.Depart;
import com.kyn.service.impl.DepartServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/provider/depart")
public class DepartController {

    @Autowired
    private DepartServiceImpl departService;

    @Autowired
    private DiscoveryClient client;

    @PostMapping("/save")
    public boolean saveHandle(@RequestBody Depart depart){
        return departService.saveDepart(depart);
    }

    @DeleteMapping("/del/{id}")
    public boolean deleteHandle(@PathVariable("id") int id){
        return departService.removeDepartById(id);
    }

    @PutMapping("/update")
    public boolean updateHandle(@RequestBody Depart depart){
        return departService.modifyDepart(depart);
    }

    @GetMapping("/get/{id}")
    public Depart getHandle(@PathVariable("id") int id){
        return departService.getDepartById(id);
    }

    @GetMapping("/list")
    public List<Depart> listHandle(){
        return departService.listAllDeparts();
    }

    @GetMapping("/discovery")
    public Object discoveryHandle(){
        //获取服务列表中的所有服务名称，即spring.application.name的值
        List<String> services = client.getServices();
        for(String name:services){
            //获取指定名称的所有服务提供者
            List<ServiceInstance> instances = client.getInstances(name);
            for(ServiceInstance  instance:instances){
                //获取服务的id 即eureka.instance.instance-id的值
                String serviceId = instance.getServiceId();
                //获取提供者的uri、主机名、端口号
                URI uri = instance.getUri();
                String host = instance.getHost();
                int port = instance.getPort();
                System.out.println(serviceId+":"+uri);
                System.out.println(host+":"+port);
            }
        }
        return services;
    }

}
