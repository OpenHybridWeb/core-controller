package com.hybridweb.core.controller.website;

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

        assertEquals(2, config.getComponents().size());
        assertEquals("/test1", config.getComponents().get(0).getContext());
        assertEquals("/test2", config.getComponents().get(1).getContext());
    }

}