package com.hybridweb.core.controller.gateway;

import com.hybridweb.core.controller.Utils;
import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.ComponentSpec;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
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
    DefaultKubernetesClient client;

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
            String targetContext = c.getSpec().getTargetContext();
            if (c.isKindGit()) {
                url = staticContentUrl;
                if (StringUtils.isEmpty(targetContext) && StringUtils.equals("/", c.getContext())) {
                    targetContext = rootContext;
                }
            } else if (c.isKindService()) {
                url = c.getSpec().getUrl();
            } else {
                throw new RuntimeException("Unknown kind: " + c.getKind());
            }
            config.addRoute(routeContext, url, targetContext);
        }
        return config;
    }

    public void updateConfigSecret(String env, String namespace, WebsiteConfig websiteConfig) {
        GatewayConfig gatewayConfig = createGatewayConfig(env, websiteConfig);
        String data = new Yaml().dumpAsMap(gatewayConfig);
        updateConfigSecret(namespace, data);
    }

    public void updateConfigSecret(String namespace, String secretData) {
        log.infof("Update core-gateway-config secret in namespace=%s\n%s", namespace, secretData);
        Map<String, String> data = new HashMap<>();
        data.put("core-gateway-config.yaml", secretData);

        SecretBuilder gatewayConfig = new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder().withName(GATEWAY_CONFIG_NAME).build())
                .withStringData(data);
        client.inNamespace(namespace).secrets().createOrReplace(gatewayConfig.build());
    }

    public void deploy(String namespace) {
        InputStream gateway = GatewayController.class.getResourceAsStream("/k8s/core-gateway.yaml");
        client.inNamespace(namespace).load(gateway).createOrReplace();

        log.info("core-gateway deployed");
    }

    public void redeploy(String namespace) {
        client.inNamespace(namespace).apps().deployments().withName("core-gateway").rolling().restart();
        log.info("core-gateway redeployed");
    }

}
