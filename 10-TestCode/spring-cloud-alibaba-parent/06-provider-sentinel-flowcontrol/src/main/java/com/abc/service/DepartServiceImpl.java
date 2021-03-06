package com.abc.service;

import com.abc.bean.Depart;
import com.abc.repository.DepartRepository;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DepartServiceImpl implements DepartService {
    @Autowired
    private DepartRepository repository;

    @Override
    public boolean saveDepart(Depart depart) {
        Depart obj = repository.save(depart);
        if(obj != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean removeDepartById(int id) {
        if(repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean modifyDepart(Depart depart) {
        Depart obj = repository.save(depart);
        if(obj != null) {
            return true;
        }
        return false;
    }

    @Override
    public Depart getDepartById(int id) {

        try {
            Thread.sleep(500);//休眠500毫秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (id ==1){
            throw new RuntimeException("There is a bugger~~~~");
        }

        if(repository.existsById(id)) {
            return repository.getOne(id);
        }
        Depart depart = new Depart();
        depart.setName("no this depart");
        return depart;
    }

    @SentinelResource(value = "qpsFlowRule",fallback = "listHandlerFallback")
    @Override
    public List<Depart> listAllDeparts() {
        return repository.findAll();
    }
    //指定服务降级方法
    public List<Depart> listHandlerFallback() {
        List<Depart> list = new ArrayList<>();
        Depart depart = new Depart();
        depart.setName("no any");
        list.add(depart);
        return list;
    }
}
