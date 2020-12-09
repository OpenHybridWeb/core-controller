package com.hybridweb.core.controller.staticcontent;

import com.hybridweb.core.controller.website.WebsiteConfigService;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StaticContentControllerTest {

    @Test
    void createConfig() throws IOException {
        InputStream is = StaticContentControllerTest.class.getResourceAsStream("/staticcontent-website-test.yaml");
        WebsiteConfig websiteConfig = WebsiteConfigService.loadYaml(is);
        is.close();

        String env = "test";

        StaticContentController controller = new StaticContentController();
        controller.rootContext = "/_root_test/";
        StaticContentConfig config = controller.createConfig(env, websiteConfig);

        assertEquals(2, config.getComponents().size());
        StaticContentConfig.StaticComponent component1 = config.getComponents().get(0);
        assertEquals("test-only-dev", component1.getDir());
        assertEquals("git", component1.getKind());
        assertEquals("giturl1", component1.getSpec().getUrl());
        assertEquals("/subidr", component1.getSpec().getDir());
        assertEquals("special-branch", component1.getSpec().getRef());

        StaticContentConfig.StaticComponent component2 = config.getComponents().get(1);
        assertEquals("_root_test/", component2.getDir());
        assertEquals("git", component2.getKind());
        assertEquals("giturl2", component2.getSpec().getUrl());
        assertEquals("/", component2.getSpec().getDir());
        assertEquals(env, component2.getSpec().getRef());


    }
}