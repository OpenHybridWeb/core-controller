package com.hybridweb.core.controller.website.model;

import java.util.List;

public class WebsiteDefaults {

    List<String> envs;

    String namespacePrefix;

    public List<String> getEnvs() {
        return envs;
    }

    public void setEnvs(List<String> envs) {
        this.envs = envs;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }
}
