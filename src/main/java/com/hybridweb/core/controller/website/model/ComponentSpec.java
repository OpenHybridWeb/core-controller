package com.hybridweb.core.controller.website.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.Map;

@RegisterForReflection
public class ComponentSpec {
    String url;
    String dir;
    String targetContext;
    List<Map<String, Object>> envs;

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

    public String getTargetContext() {
        return targetContext;
    }

    public void setTargetContext(String targetContext) {
        this.targetContext = targetContext;
    }

    public List<Map<String, Object>> getEnvs() {
        return envs;
    }

    public void setEnvs(List<Map<String, Object>> envs) {
        this.envs = envs;
    }
}
