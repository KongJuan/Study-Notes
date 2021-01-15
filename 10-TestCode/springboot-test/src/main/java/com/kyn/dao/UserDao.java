package com.kyn.dao;

import com.kyn.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public int addUser(User user){
        return jdbcTemplate.update("insert into userinfo(name,age) values(?,?) ",
                                  user.getName(),user.getAge());
    }

    public User getUserByName(String name){
        return jdbcTemplate.queryForObject("select * from userinfo where name=?",
                                           new BeanPropertyRowMapper<>(User.class),name);
    }

    public List<User> getAllBooks(){
        return jdbcTemplate.query("select * from userinfo"
                                        ,new BeanPropertyRowMapper(User.class));
    }

}
