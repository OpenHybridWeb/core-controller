package com.hybridweb.core.controller.website.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Environment {

    String name;

    String namespace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
