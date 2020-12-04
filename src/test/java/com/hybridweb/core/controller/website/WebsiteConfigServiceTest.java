package com.hybridweb.core.controller.website;

import com.hybridweb.core.controller.website.model.WebsiteConfig;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebsiteConfigServiceTest {

    @Test
    public void loadYaml() {
        InputStream is = WebsiteConfigServiceTest.class.getResourceAsStream("/website-test.yaml");
        WebsiteConfig config = WebsiteConfigService.loadYaml(is);
        assertEquals(3, config.getDefaults().getEnvs().size());
        assertEquals(2, config.getComponents().size());
        assertEquals("/test1", config.getComponents().get(0).getContext());
    }

}