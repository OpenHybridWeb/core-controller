package com.hybridweb.core.controller.website;

import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebsiteConfigServiceTest {

    @Test
    public void loadYaml() throws IOException {
        InputStream is = WebsiteConfigServiceTest.class.getResourceAsStream("/website-test.yaml");
        WebsiteConfig config = WebsiteConfigService.loadYaml(is);
        is.close();

        assertEquals(3, config.getEnvs().size());
        assertEquals("ns-1", config.getEnvironment("dev").getNamespace());

        assertEquals(3, config.getComponents().size());
        assertEquals("/test1", config.getComponents().get(0).getContext());
        assertEquals(true, config.getComponents().get(0).isKindGit());
        assertEquals("/test2", config.getComponents().get(1).getContext());

        ComponentConfig config3 = config.getComponents().get(2);
        assertEquals("/test3", config3.getContext());
        assertEquals("service", config3.getKind());
        assertEquals(true, config3.isKindService());
        assertEquals("api", config3.getSpec().getServiceName());
        assertEquals("80", config3.getSpec().getTargetPort());
    }

}