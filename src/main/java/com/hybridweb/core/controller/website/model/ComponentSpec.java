package com.hybridweb.core.controller.website.model;

import java.util.List;
import java.util.Map;

public class ComponentSpec {
    String url;
    String dir;
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

    public List<Map<String, Object>> getEnvs() {
        return envs;
    }

    public void setEnvs(List<Map<String, Object>> envs) {
        this.envs = envs;
    }
}
