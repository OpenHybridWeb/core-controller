package com.hybridweb.core.controller.gateway;

import java.util.ArrayList;
import java.util.List;

public class GatewayConfig {
    List<Route> routes = new ArrayList<>();

    public void addRoute(String context, String url) {
        routes.add(new Route(context, url));
    }

    public class Route {
        String context;
        String url;

        public Route() {
        }

        public Route(String context, String url) {
            this.context = context;
            this.url = url;
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
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
}
