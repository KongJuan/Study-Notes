package com.kyn.rule;

import com.netflix.eureka.registry.rule.InstanceStatusOverrideRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;

public class CustomRule implements IRule {
    @Override
    public Server choose(Object o) {
        return null;
    }

    @Override
    public void setLoadBalancer(ILoadBalancer iLoadBalancer) {
        
    }

    @Override
    public ILoadBalancer getLoadBalancer() {
        return null;
    }
}
