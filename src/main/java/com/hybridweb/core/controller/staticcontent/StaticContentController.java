package com.hybridweb.core.controller.staticcontent;

import com.hybridweb.core.controller.Utils;
import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.ComponentSpec;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class StaticContentController {

    private static final Logger log = Logger.getLogger(StaticContentController.class);

    static final String GATEWAY_CONFIG_NAME = "core-staticcontent-config";

    static final String STATIC_CONTENT_API_HOST = "localhost";
    static final int STATIC_CONTENT_API_PORT = 57846;

    @Inject
    DefaultKubernetesClient client;

    @Inject
    Vertx vertx;

    HttpClient staticContentClient;

    @ConfigProperty(name = "app.staticcontent.rootcontext")
    protected String rootContext;

    void onStart(@Observes StartupEvent ev) throws GitAPIException, IOException {
        staticContentClient = vertx.createHttpClient(new HttpClientOptions()
                .setDefaultPort(STATIC_CONTENT_API_PORT)
                .setDefaultHost(STATIC_CONTENT_API_HOST)
        );
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
        log.infof("Update core-staticcontent-config secret \n%s", secretData);
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

    public void refreshComponent(String name) {
        staticContentClient.get("/_staticcontent/api/update/" + name).handler(ar -> {
            if (ar.statusCode()  == 200) {
                log.info("core-staticcontent refreshed");
            } else {
                log.infof("core-staticcontent cannot refresh error=%s", ar.statusMessage());
            }
        }).end();
    }

}
