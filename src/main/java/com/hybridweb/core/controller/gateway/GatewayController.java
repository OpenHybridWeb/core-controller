package com.hybridweb.core.controller.gateway;

import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class GatewayController {

    private static final Logger log = Logger.getLogger(GatewayController.class);


    static final String GATEWAY_CONFIG_NAME = "core-gateway-config";

    @ConfigProperty(name = "app.staticcontent.url")
    protected String staticContentUrl;

    @Inject
    KubernetesClient client;

    public GatewayConfig createGatewayConfig(WebsiteConfig websiteConfig) {
        GatewayConfig gatewayConfig = new GatewayConfig();
        websiteConfig.getComponents().forEach(c -> gatewayConfig.addRoute(c.getContext(), staticContentUrl));
        return gatewayConfig;
    }

    public void updateConfigSecret(WebsiteConfig websiteConfig) {
        GatewayConfig gatewayConfig = createGatewayConfig(websiteConfig);
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        String data= yaml.dump(gatewayConfig);
        updateConfigSecret(data );
    }

    public void updateConfigSecret(String secretData) {
        Map<String, String> data = new HashMap<>();
        data.put("static-content-config.yaml", secretData);

        SecretBuilder gatewayConfig = new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder().withName(GATEWAY_CONFIG_NAME).build())
                .withStringData(data);
        client.secrets().createOrReplace(gatewayConfig.build());
    }

    public void deployGateway() {
    }

}
