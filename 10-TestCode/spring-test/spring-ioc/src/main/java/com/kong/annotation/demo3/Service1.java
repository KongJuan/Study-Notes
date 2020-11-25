package com.kong.annotation.demo3;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@Qualifier("tag1")
public class Service1 implements IService{
}
