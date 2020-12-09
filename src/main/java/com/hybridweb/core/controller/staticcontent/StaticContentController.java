package com.hybridweb.core.controller.staticcontent;

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
public class StaticContentController {

    private static final Logger log = Logger.getLogger(StaticContentController.class);

    static final String GATEWAY_CONFIG_NAME = "core-staticcontent-config";

    @Inject
    KubernetesClient client;

    @ConfigProperty(name = "app.staticcontent.rootcontext")
    protected String rootContext;

    public StaticContentConfig createConfig(String targetEnv, WebsiteConfig websiteConfig) {
        StaticContentConfig config = new StaticContentConfig();
        if (!Utils.isEnvEnabled(websiteConfig.getDefaults(), targetEnv)) {
            return config;
        }
        for (ComponentConfig c : websiteConfig.getComponents()) {
            ComponentSpec spec = c.getSpec();
            List<Map<String, Object>> envs = spec.getEnvs();
            if (!Utils.isEnvIncluded(envs, targetEnv)) {
                continue;
            }

            if (c.getKind().equals("git")) {
                String dir = c.getContext();
                if (StringUtils.equals("/", c.getContext())) {
                    dir = rootContext;
                }
                dir = dir.substring(1); // remove starting "/"
                String gitDir = StringUtils.defaultIfEmpty(spec.getDir(), "/");
                config.addGitComponent(dir, c.getKind(), spec.getUrl(), getRef(envs, targetEnv), gitDir);
            }
        }
        return config;
    }

    public String getRef(List<Map<String, Object>> envs, String targetEnv) {
        if (envs == null) {
            return targetEnv;
        }
        for (Map<String, Object> env : envs) {
            return (String) env.get(targetEnv);
        }
        return targetEnv;
    }

    public void updateConfigSecret(String env, WebsiteConfig websiteConfig) {
        StaticContentConfig config = createConfig(env, websiteConfig);
        String data = new Yaml().dumpAsMap(config);
        updateConfigSecret(data);
    }

    public void updateConfigSecret(String secretData) {
        log.infof("Update core-staticcontent-config secret \n%s", secretData);
        Map<String, String> data = new HashMap<>();
        data.put("core-staticcontent-config.yaml", secretData);

        SecretBuilder config = new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder().withName(GATEWAY_CONFIG_NAME).build())
                .withStringData(data);
        client.secrets().createOrReplace(config.build());
    }

    public void deploy() {
        InputStream service = StaticContentController.class.getResourceAsStream("/k8s/core-staticcontent.yaml");
        client.load(service).createOrReplace();

        log.infof("core-staticcontent deployed");
    }

}
