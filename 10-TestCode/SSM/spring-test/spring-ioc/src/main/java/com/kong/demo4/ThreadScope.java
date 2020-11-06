package com.kong.demo4;

import sun.reflect.generics.scope.Scope;

import java.lang.reflect.TypeVariable;

public class ThreadScope implements Scope {
    @Override
    public TypeVariable<?> lookup(String s) {
        return null;
    }
}
