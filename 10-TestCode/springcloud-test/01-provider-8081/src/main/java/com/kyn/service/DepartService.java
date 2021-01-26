package com.kyn.service;

import com.kyn.pojo.Depart;

import java.util.List;

public interface DepartService {

    boolean saveDepart(Depart depart);
    boolean removeDepartById(int id);
    boolean modifyDepart(Depart depart);
    Depart getDepartById(int id);
    List<Depart> listAllDeparts();

}
