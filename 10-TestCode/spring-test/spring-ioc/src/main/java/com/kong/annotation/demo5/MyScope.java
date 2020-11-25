package com.kong.annotation.demo5;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(BeanMyScope.SCOPE_MY)
public @interface MyScope {
    ScopedProxyMode proxyMode() default  ScopedProxyMode.TARGET_CLASS;
}
