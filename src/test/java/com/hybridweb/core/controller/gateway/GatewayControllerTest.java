package com.hybridweb.core.controller.gateway;

import com.hybridweb.core.controller.website.WebsiteConfigService;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GatewayControllerTest {

    @Test
    void createGatewayConfig() throws IOException {
        InputStream is = GatewayControllerTest.class.getResourceAsStream("/gateway-website-test.yaml");
        WebsiteConfig websiteConfig = WebsiteConfigService.loadYaml(is);
        is.close();

        String staticContentUrl = "http://sc";

        GatewayController gatewayController= new GatewayController();
        gatewayController.staticContentUrl = staticContentUrl;
        gatewayController.rootContext = "/_test_root/";
        GatewayConfig config = gatewayController.createGatewayConfig(websiteConfig);

        assertEquals(3, config.getRoutes().size());
        GatewayConfig.Route route1 = config.getRoutes().get(0);
        assertEquals("/test-only-dev/*", route1.getContext());
        assertEquals(staticContentUrl, route1.getUrl());

        GatewayConfig.Route route2 = config.getRoutes().get(1);
        assertEquals("/test-minimal/*", route2.getContext());
        assertEquals(staticContentUrl, route2.getUrl());

        GatewayConfig.Route route3 = config.getRoutes().get(2);
        assertEquals("/*", route3.getContext());
        assertEquals("/_test_root/", route3.getTargetContext());
        assertEquals(staticContentUrl, route2.getUrl());

    }
}