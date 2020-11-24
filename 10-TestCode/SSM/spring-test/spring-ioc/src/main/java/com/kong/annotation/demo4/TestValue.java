package com.kong.annotation.demo4;

import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

public class TestValue {
    /**
     * 解析@Value的过程：
     * 1. 将@Value注解的value参数值作为Environment.resolvePlaceholders方法参数进行解析
     * 2. Environment内部会访问MutablePropertySources来解析
     * 3. MutablePropertySources内部有多个PropertySource，此时会遍历PropertySource列表，
     * 调用 PropertySource.getProperty方法来解析key对应的值
     */
    @Test
    public void test01(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        //模拟从db中获取配置信息
        Map<String,Object> mainInfoFromDb=DbUtil.getMailInfoFromDb();
        //将其丢再MapPropertySource中
        // （MapPropertySource类是spring提供的一个类，是 PropertySource的子类）
        MapPropertySource mapPropertySource=new MapPropertySource("mail",mainInfoFromDb);
        //将mailPropertySource丢在Environment中的PropertySource列表的第一个中，让优先级最高
        context.getEnvironment().getPropertySources().addFirst(mapPropertySource);

        context.register(MainConfig2.class);
        context.refresh();
        MailConfig config=context.getBean(MailConfig.class);
        System.out.println(config);
    }
}
