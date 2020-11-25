package com.kong.annotation.demo4;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DbUtil {
    public static Map<String,Object> getMailInfoFromDb(){
        Map<String,Object> result=new HashMap<>();
        result.put("mail.host", "smtp.qq.com");
        result.put("mail.username", "路人");
        result.put("mail.password", "123");
        return result;
    }
}
