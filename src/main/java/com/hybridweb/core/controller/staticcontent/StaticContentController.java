package com.hybridweb.core.controller.staticcontent;

import com.hybridweb.core.controller.Utils;
import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.ComponentSpec;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
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
    DefaultKubernetesClient client;

    @Inject
    Vertx vertx;

    WebClient staticContentClient;

    @ConfigProperty(name = "app.staticcontent.api.host")
    String staticContentHost;

    @ConfigProperty(name = "app.staticcontent.api.port")
    int staticContentApiPort;

    @ConfigProperty(name = "app.staticcontent.rootcontext")
    protected String rootContext;

    void onStart(@Observes StartupEvent ev){
        this.staticContentClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost(staticContentHost)
                .setDefaultPort(staticContentApiPort)
                .setTrustAll(true));
        log.infof("Static content client created host=%s port=%s", staticContentHost, staticContentApiPort);
    }

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

    public void updateConfigSecret(String env, String namespace, WebsiteConfig websiteConfig) {
        StaticContentConfig config = createConfig(env, websiteConfig);
        String data = new Yaml().dumpAsMap(config);
        updateConfigSecret(namespace, data);
    }

    public void updateConfigSecret(String namespace, String secretData) {
        log.infof("Update core-staticcontent-config secret in namespace=%s\n%s", namespace, secretData);
        Map<String, String> data = new HashMap<>();
        data.put("core-staticcontent-config.yaml", secretData);

        SecretBuilder config = new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder().withName(GATEWAY_CONFIG_NAME).build())
                .withStringData(data);
        client.inNamespace(namespace).secrets().createOrReplace(config.build());
    }

    public void deploy(String namespace) {
        InputStream service = StaticContentController.class.getResourceAsStream("/k8s/core-staticcontent.yaml");
        client.inNamespace(namespace).load(service).createOrReplace();

        log.info("core-staticcontent deployed");
    }

    public void redeploy(String namespace) {
        client.inNamespace(namespace).apps().deployments().withName("core-staticcontent").rolling().restart();
        log.info("core-staticcontent redeployed");
    }

    public Uni<JsonObject> refreshComponent(String name) {
        log.infof("Refresh component name=%s", name);
        return staticContentClient.get("/_staticcontent/api/update/" + name).send().map(resp -> {
            if (resp.statusCode() == 200) {
                return resp.bodyAsJsonObject();
            } else {
                return new JsonObject()
                        .put("code", resp.statusCode())
                        .put("message", resp.bodyAsString());
            }
        });
    }

    public Uni<JsonObject> listComponents() {
        log.infof("List components");
        return staticContentClient.get("/_staticcontent/api/list").send().map(resp -> {
            if (resp.statusCode() == 200) {
                return resp.bodyAsJsonObject();
            } else {
                return new JsonObject()
                        .put("code", resp.statusCode())
                        .put("message", resp.bodyAsString());
            }
        });
    }

}
