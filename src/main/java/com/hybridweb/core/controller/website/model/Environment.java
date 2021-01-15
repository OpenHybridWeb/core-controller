package com.hybridweb.core.controller.website.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Environment {

    String namespace;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
