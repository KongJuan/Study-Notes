package com.kyn.acutuator.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MyComHealthIndicator extends AbstractHealthIndicator {
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        Map<String,Object> map=new HashMap<>();
        if(1==1){
            //builder.up(); //健康
            builder.status(Status.UP);
            map.put("code",10000);
            map.put("msg","健康");
        }else{
            //builder.down();//不健康
            builder.status(Status.OUT_OF_SERVICE);
            map.put("code",00000);
            map.put("msg","不健康");
        }
        builder.withDetails(map);
    }
}
