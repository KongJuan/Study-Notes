package com.kyn;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

@Slf4j
@SpringBootTest
class SpringbootTestApplicationTests {

    @Autowired
    DataSource dataSource;

    @Test
    void contextLoads() {
      log.info("数据源类型：{}",dataSource.getClass());
    }

    @Test
    @DisplayName("异常测试")
    public void exceptionTest() {
        ArithmeticException exception = Assertions.assertThrows(
                //扔出断言异常
                ArithmeticException.class, () -> System.out.println(1 % 0));

    }

}
