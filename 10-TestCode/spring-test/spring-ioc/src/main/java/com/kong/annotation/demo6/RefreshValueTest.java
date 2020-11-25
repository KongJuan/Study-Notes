package com.kong.annotation.demo6;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.concurrent.TimeUnit;

public class RefreshValueTest {
    @Test
    public void test01() throws InterruptedException {
        AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerScope(BeanRefreshScope.SCOPE_REFRESH, BeanRefreshScope.getInstance());
        context.register(MainConfig.class);
        //刷新mail的配置到Environment
        RefreshConfigUtil.refreshMailPropertySource(context);
        context.refresh();
        MailService mailService = context.getBean(MailService.class);
        System.out.println("配置未更新的情况下,输出3次");
        for (int i = 0; i < 3; i++) {
            System.out.println(mailService);
            TimeUnit.MILLISECONDS.sleep(200);
        }
        System.out.println("模拟3次更新配置效果");
        for (int i = 0; i < 3; i++) {
            RefreshConfigUtil.updateDbConfig(context);
            System.out.println(mailService);
            TimeUnit.MILLISECONDS.sleep(200);

        }
    }
}
