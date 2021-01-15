package com.hybridweb.core.controller.staticcontent;

import com.hybridweb.core.controller.Utils;
import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.ComponentSpec;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
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
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class StaticContentController {

    private static final Logger log = Logger.getLogger(StaticContentController.class);

    static final String STATIC_CONFIG_NAME = "static-config-";
    static final String STATIC_CONFIG_HTTPD_NAME = "static-config-httpd-";

    @Inject
    DefaultOpenShiftClient client;

    @Inject
    Vertx vertx;

    WebClient staticContentClient;

    @ConfigProperty(name = "app.staticcontent.api.host")
    String staticContentHost;

    @ConfigProperty(name = "app.staticcontent.api.port")
    int staticContentApiPort;

    @ConfigProperty(name = "app.staticcontent.rootcontext")
    protected String rootContext;

    void onStart(@Observes StartupEvent ev) {
        this.staticContentClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost(staticContentHost)
                .setDefaultPort(staticContentApiPort)
                .setTrustAll(true));
        log.infof("Static content client created host=%s port=%s", staticContentHost, staticContentApiPort);
    }

    public StaticContentConfig createConfig(String targetEnv, WebsiteConfig websiteConfig) {
        StaticContentConfig config = new StaticContentConfig();
        if (!Utils.isEnvEnabled(websiteConfig, targetEnv)) {
            return config;
        }
        for (ComponentConfig c : websiteConfig.getComponents()) {
            ComponentSpec spec = c.getSpec();
            Map<String, Map<String, Object>> envs = spec.getEnvs();
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

    public StringBuffer createAliases(String targetEnv, WebsiteConfig websiteConfig) {
        StringBuffer config = new StringBuffer();
        if (!Utils.isEnvEnabled(websiteConfig, targetEnv)) {
            return config;
        }
        for (ComponentConfig c : websiteConfig.getComponents()) {
            ComponentSpec spec = c.getSpec();
            if (!Utils.isEnvIncluded(spec.getEnvs(), targetEnv)) {
                continue;
            }

            if (c.getKind().equals("git")) {
                String dir = c.getContext();
                if (StringUtils.equals("/", c.getContext())) {
                    continue;
                }
                config.append("Alias " + dir + " /var/www/components" + dir);
            }
        }
        return config;
    }

    public String getRef(Map<String, Map<String, Object>> envs, String targetEnv) {
        if (envs == null) {
            return targetEnv;
        }
        Map<String, Object> env = envs.get(targetEnv);
        if (env == null) {
            // no override - branch is same as env
            return targetEnv;
        }
        return (String) env.get("branch");
    }

    public void updateConfigs(String env, String namespace, WebsiteConfig websiteConfig) {
        StaticContentConfig config = createConfig(env, websiteConfig);
        String data = new Yaml().dumpAsMap(config);
        updateConfigSecret(env, namespace, data);

        String aliases = createAliases(env, websiteConfig).toString();
        updateConfigHttpdSecret(env, namespace, aliases);
    }

    public void updateConfigSecret(String env, String namespace, String secretData) {
        String name = STATIC_CONFIG_NAME + env;
        log.infof("Update static-config in namespace=%s, name=%s\n%s", namespace, name, secretData);

        Map<String, String> data = new HashMap<>();
        data.put("core-staticcontent-config.yaml", secretData);

        SecretBuilder config = new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder().withName(name).build())
                .withStringData(data);
        client.inNamespace(namespace).secrets().createOrReplace(config.build());
    }

    public void updateConfigHttpdSecret(String env, String namespace, String aliasesData) {
        String name = STATIC_CONFIG_HTTPD_NAME + env;
        log.infof("Update static-config-httpd in namespace=%s, name=%s\n%s", namespace, name, aliasesData);

        Map<String, String> dataAlias = new HashMap<>();
        dataAlias.put("aliases.conf", aliasesData);

        SecretBuilder configAlias = new SecretBuilder()
                .withMetadata(new ObjectMetaBuilder().withName(name).build())
                .withStringData(dataAlias);
        client.inNamespace(namespace).secrets().createOrReplace(configAlias.build());
    }


    public void deploy(String env, String namespace) {
        final Template serverUploadedTemplate = client.templates()
                .inNamespace(namespace)
                .load(StaticContentController.class.getResourceAsStream("/k8s/core-staticcontent.yaml"))
                .createOrReplace();
        String templateName = serverUploadedTemplate.getMetadata().getName();
        log.infof("Template %s successfully created on server, namespace=%s", serverUploadedTemplate.getMetadata().getName(), namespace);

        Map<String, String> params = new HashMap<>();
        params.put("ENV", env);

        KubernetesList result = client.templates()
                .inNamespace(namespace).withName(templateName)
                .process(params);

        log.debugf("Template %s successfully processed to list with %s items",
                result.getItems().get(0).getMetadata().getName(),
                result.getItems().size());

        for (HasMetadata item : result.getItems()) {
            log.infof("Deploying kind=%s name=%s", item.getKind(), item.getMetadata().getName());
            // see https://www.javatips.net/api/fabric8-master/components/kubernetes-api/src/main/java/io/fabric8/kubernetes/api/Controller.java#
            if (item instanceof Service) {
                client.inNamespace(namespace).services().createOrReplace((Service) item);
            }
            if (item instanceof Deployment) {
                client.inNamespace(namespace).apps().deployments().createOrReplace((Deployment) item);
            }

        }
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
