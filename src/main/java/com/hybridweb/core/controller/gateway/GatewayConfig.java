package com.hybridweb.core.controller.gateway;

import java.util.LinkedList;
import java.util.List;

public class GatewayConfig {
    List<Route> routes = new LinkedList<>();

    /**
     * Add route
     *
     * @param context       incoming context
     * @param url           target url
     * @param targetContext context is replaced by targetContext
     */
    public void addRoute(String context, String url, String targetContext) {
        routes.add(new Route(context, url, targetContext));
    }

    public class Route {
        String context;
        String url;
        String targetContext;

        public Route() {
        }

        public Route(String context, String url) {
            this.context = context;
            this.url = url;
        }

        public Route(String context, String url, String targetContext) {
            this(context, url);
            this.targetContext = targetContext;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTargetContext() {
            return targetContext;
        }

        public void setTargetContext(String targetContext) {
            this.targetContext = targetContext;
        }
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
}
