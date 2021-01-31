package com.kyn.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Entity //自动建表
//JPA的默认实现是Hibernate，而Hibernate默认对于对象的查询是基于延迟加载的
@JsonIgnoreProperties({"hibernateLazyInitializer","handler","fieldHandler"})
public class Depart{

    @Id //表示当前属性为主键
    @GeneratedValue(strategy = GenerationType.IDENTITY) //表示主键自动递增
    private  Integer id;
    private String depNo;
    private String depName;
}
