package com.kyn.acutuator.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@Endpoint(id="myEndpoint")
public class MyEndpoint {
    @ReadOperation
    public Map getMyServerInfo(){
        return Collections.singletonMap("info","MyServer tarted...");
    }

    @WriteOperation
    private void restartMyServer(){
        System.out.println("MyServer restarted....");
    }
}
