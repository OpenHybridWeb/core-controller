package com.hybridweb.core.controller.website.model;

import java.util.List;

public class WebsiteConfig {

    String apiVersion;

    WebsiteDefaults websiteDefaults;

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

    public WebsiteDefaults getDefaults() {
        return websiteDefaults;
    }

    public void setDefaults(WebsiteDefaults websiteDefaults) {
        this.websiteDefaults = websiteDefaults;
    }
}
