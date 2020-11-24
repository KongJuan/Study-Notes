package com.kong.annotation.demo5;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestScope {
    @Test
    public void test3() throws InterruptedException
    {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        //将自定义作用域注册到spring容器中
        context.getBeanFactory().registerScope(BeanMyScope.SCOPE_MY, new BeanMyScope());
        context.register(MainConfig.class);
        context.refresh();
        System.out.println("从容器中获取User对象");

        User user = context.getBean(User.class);

        System.out.println("user对象的class为：" + user.getClass());
        System.out.println("多次调用user的getUsername感受一下效果\n");
        for (int i = 1; i <= 3; i++) {
            System.out.println(String.format("********\n第%d次开始调用getUsername", i));
            System.out.println(user.getUsername());
            System.out.println(String.format("第%d次调用getUsername结束\n********\n", i));
        }
    }
}
