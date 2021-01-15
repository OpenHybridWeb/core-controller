package com.hybridweb.core.controller.website.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

@RegisterForReflection
public class ComponentSpec {
    String url;
    String dir;

    String serviceName;
    String targetPort;

    Map<String, Map<String, Object>> envs;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTargetPort() {
        return targetPort;
    }

    public void setTargetPort(String targetPort) {
        this.targetPort = targetPort;
    }

    public Map<String, Map<String, Object>> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, Map<String, Object>> envs) {
        this.envs = envs;
    }
}
