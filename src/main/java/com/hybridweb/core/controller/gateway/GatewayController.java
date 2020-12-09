package com.hybridweb.core.controller.gateway;

import com.hybridweb.core.controller.Utils;
import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.ComponentSpec;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GatewayController {

    private static final Logger log = Logger.getLogger(GatewayController.class);

    static final String GATEWAY_CONFIG_NAME = "core-gateway-config";

    @ConfigProperty(name = "app.staticcontent.url")
    protected String staticContentUrl;

    @ConfigProperty(name = "app.staticcontent.rootcontext")
    protected String rootContext;

    @Inject
    KubernetesClient client;

    public GatewayConfig createGatewayConfig(String targetEnv, WebsiteConfig websiteConfig) {
        GatewayConfig config = new GatewayConfig();
        if (!Utils.isEnvEnabled(websiteConfig.getDefaults(), targetEnv)) {
            return config;
        }
        for (ComponentConfig c : websiteConfig.getComponents()) {
            ComponentSpec spec = c.getSpec();
            List<Map<String, Object>> envs = spec.getEnvs();
            if (!Utils.isEnvIncluded(envs, targetEnv)) {
                continue;
            }
            String routeContext = c.getContext() + "*";
            String url;
            if (c.isKindGit()) {
                url = staticContentUrl;
            } else if (c.isKindService()) {
                url = c.getSpec().getUrl();
            } else {
                throw new RuntimeException("Unknown kind: " + c.getKind());
            }
            if (StringUtils.equals("/", c.getContext())) {
                config.addRoute(routeContext, url, rootContext);
            } else {
                config.addRoute(routeContext, url);
            }
        }
        return config;
    }

    public void updateConfigSecret(String env, WebsiteConfig websiteConfig) {
        GatewayConfig gatewayConfig = createGatewayConfig(env, websiteConfig);
        String data = new Yaml().dumpAsMap(gatewayConfig);
        updateConfigSecret(data);
    }

    public void updateConfigSecret(String secretData) {
        log.infof("Update config secret \n%s", secretData);
        Map<String, String> data = new HashMap<>();
        data.put("core-gateway-config.yaml", secretData);

        SecretBuilder gatewayConfig = new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder().withName(GATEWAY_CONFIG_NAME).build())
                .withStringData(data);
        client.secrets().createOrReplace(gatewayConfig.build());
    }

    public void deploy() {
        InputStream gateway = GatewayController.class.getResourceAsStream("/k8s/core-gateway.yaml");
        client.load(gateway).createOrReplace();

        log.info("core-gateway deployed");
    }

    public void redeploy() {
        client.apps().deployments().withName("core-gateway").rolling().restart();
        log.info("core-gateway redeployed");
    }

}
