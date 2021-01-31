package com.kyn.service.impl;

import com.kyn.pojo.Depart;
import com.kyn.repository.DepartRepository;
import com.kyn.service.DepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartServiceImpl implements DepartService {

    @Autowired
    public DepartRepository departRepository;
    @Value("${server.port}")
    private int port;

    @Override
    public boolean saveDepart(Depart depart) {
        //对于save的参数，根据id的不同，有以下三种情况：
        //1.depart的id是null,且DB中该id存在,save()执行的是参入操作
        //2.depart的id不为null,且DB中id存在，save()执行的是修改操作
        //3.depart的id不为null，且DB中该id不存在，save()执行的是插入操作，但其插入的id是根据id的生成策略生成的。
        Depart obj = departRepository.save(depart);
        return obj != null ? true : false;
    }

    @Override
    public boolean removeDepartById(int id) {

        if(departRepository.existsById(id)){
            //在DB中指定的id不存在则会抛出异常
            departRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean modifyDepart(Depart depart) {
        Depart obj = departRepository.save(depart);
        return obj != null ? true : false;
    }

    @Override
    public Depart getDepartById(int id) {
        if(departRepository.existsById(id)){
            //指定id的实体不存在，getOne()方法会抛异常
            Depart depart = departRepository.getOne(id);
            depart.setDepName(depart.getDepName()+port);
            return depart;
        }
        return null;
    }

    @Override
    public List<Depart> listAllDeparts() {

        return departRepository.findAll();
    }
}
