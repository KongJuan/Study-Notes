package com.kyn.repository;

import com.kyn.pojo.Depart;
import org.springframework.data.jpa.repository.JpaRepository;

//第一个泛型表示要操作对象的类型
//第二个泛型表示要操作对象的id类型
public interface DepartRepository extends JpaRepository<Depart,Integer> {
}
