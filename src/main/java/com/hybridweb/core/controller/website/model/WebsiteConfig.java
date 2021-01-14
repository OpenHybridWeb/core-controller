package com.hybridweb.core.controller.website.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection
public class WebsiteConfig {

    String apiVersion;

    List<Environment> envs;

    List<ComponentConfig> components;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public List<ComponentConfig> getComponents() {
        return components;
    }

    public void setComponents(List<ComponentConfig> components) {
        this.components = components;
    }

    public List<Environment> getEnvs() {
        return envs;
    }

    public void setEnvs(List<Environment> envs) {
        this.envs = envs;
    }

    public Environment getEnvironment(String envName) {
        for (Environment env : getEnvs()) {
            if (env.getName().equals(envName)) {
                return env;
            }
        }
        return null;
    }
}
