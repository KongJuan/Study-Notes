package com.kyn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kyn.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserMapper extends BaseMapper<User> {

}
