package com.navercorp.pinpoint.profiler.interceptor.bci;

import com.navercorp.pinpoint.bootstrap.instrument.ScopeDefinition;

/**
 * @author emeroad
 */
public class DefaultScopeDefinition implements ScopeDefinition {

    private final String name;
    private final Type type;

    public DefaultScopeDefinition(String name, Type type) {
        if (name == null) {
            throw new NullPointerException("name must not be null");
        }
        if (type == null) {
            throw new NullPointerException("scopeType must not be null");
        }
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }
}