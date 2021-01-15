package com.kyn.mapper;

import com.kyn.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserMapper {

    //@Select("select * from userinfo where name=#{name}")
    public User getUser(String name);
}
