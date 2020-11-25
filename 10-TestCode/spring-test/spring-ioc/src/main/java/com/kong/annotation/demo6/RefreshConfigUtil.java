package com.kong.annotation.demo6;

import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RefreshConfigUtil {
    /**
     * 模拟改变数据库中都配置信息
     */
    public static void updateDbConfig(AbstractApplicationContext context){
        //更新context中的mailPropertySource配置信息
        refreshMailPropertySource(context);
        //清空BeanRefreshScope中所有bean的缓存
        BeanRefreshScope.getInstance().clean();
    }

    public static void refreshMailPropertySource(AbstractApplicationContext context){
        Map<String,Object> mailInfoFromDb=DbUtil.getMailInforFtomDb();
        //将其丢在MapPropertySource中
        MapPropertySource propertySource=new MapPropertySource("mail",mailInfoFromDb);
        context.getEnvironment().getPropertySources().addFirst(propertySource);
    }
}
