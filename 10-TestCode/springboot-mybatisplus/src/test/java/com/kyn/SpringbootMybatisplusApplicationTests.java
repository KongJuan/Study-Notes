package com.kyn;

import com.kyn.mapper.UserMapper;
import com.kyn.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringbootMybatisplusApplicationTests {

	@Autowired
	UserMapper userMapper;
	@Test
	void contextLoads() {
	}

	@Test
	void testUserMapper(){
		User user=userMapper.selectById(1);
		System.out.println(user);
	}

}
