package com.kong.annotation.demo6;

import java.util.*;

public class DbUtil {
    //模拟从db中获取邮件配置信息

    public static Map<String,Object> getMailInforFtomDb(){
        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("mail.username", UUID.randomUUID().toString());
        return resultMap;
    }
}
