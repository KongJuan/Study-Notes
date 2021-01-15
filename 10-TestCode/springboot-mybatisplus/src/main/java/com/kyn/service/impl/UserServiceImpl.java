package com.kyn.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyn.mapper.UserMapper;
import com.kyn.pojo.User;
import com.kyn.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
