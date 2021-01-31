package com.kyn.fallback;

import com.kyn.pojo.Depart;
import com.kyn.service.DepartService;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DepartFallbackFactory implements FallbackFactory<DepartService> {
    @Override
    public DepartService create(Throwable cause) {
        return new DepartService() {
            @Override
            public boolean saveDepart(Depart depart) {
                return false;
            }

            @Override
            public boolean removeDepartById(int id) {
                return false;
            }

            @Override
            public boolean modifyDepart(Depart depart) {
                return false;
            }

            @Override
            public Depart getDepartById(int id) {
                Depart depart=new Depart();
                depart.setId(id);
                depart.setDepName("no this depart");
                return depart;
            }

            @Override
            public List<Depart> listAllDeparts() {
                return null;
            }
        };
    }
}
