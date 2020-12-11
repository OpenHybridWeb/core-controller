package com.hybridweb.core.controller;

import com.hybridweb.core.controller.gateway.GatewayController;
import com.hybridweb.core.controller.staticcontent.StaticContentController;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.rbac.*;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MainController {

    private static final Logger log = Logger.getLogger(MainController.class);

    @ConfigProperty(name = "app.controller.namespaces.defaultprefix")
    String namespacePrefix;

    @Inject
    GatewayController gatewayController;

    @Inject
    StaticContentController staticContentController;


    @Inject
    DefaultKubernetesClient client;

    String nameSpaceLabelValue = "openhybridweb";
    String CONTROLLER_CONFIG_NAME = "core-controller-config";

    public void createNamespaces(List<String> envs) {
        for (String env : envs) {
            String name = getNameSpaceName(env);
            Namespace ns = new NamespaceBuilder().withNewMetadata().withName(name).addToLabels("app", nameSpaceLabelValue).endMetadata().build();
            client.namespaces().withName(name).createOrReplace(ns);
            log.infof("Namespace %s created", name);
        }
    }

    public String getNameSpaceName(String env) {
        return namespacePrefix + env;
    }

    public void deployController(String env, String gitUrl) {
        InputStream service = StaticContentController.class.getResourceAsStream("/k8s/core-controller.yaml");
        String namespace = getNameSpaceName(env);

        updateServiceAccount(env);
        updateControllerConfig(env, gitUrl);

        client.inNamespace(namespace).load(service).createOrReplace();
        log.infof("Controller created in namespace=%s", namespace);
    }

    public void updateControllerConfig(String env, String gitUrl) {
        String namespace = getNameSpaceName(env);

        log.infof("Update core-controller-config namespace=%s", namespace);
        Map<String, String> data = new HashMap<>();
        data.put("APP_CONTROLLER_ENV", env);
        data.put("APP_CONTROLLER_WEBSITE_URL", gitUrl);

        ConfigMapBuilder configMap = new ConfigMapBuilder()
                .withMetadata(new ObjectMetaBuilder().withName(CONTROLLER_CONFIG_NAME).build())
                .withData(data);
        client.inNamespace(namespace).configMaps().createOrReplace(configMap.build());
    }

    public void updateServiceAccount(String env) {
        String namespace = getNameSpaceName(env);
        log.infof("Update service-account namespace=%s", namespace);
        ServiceAccountBuilder saBuilder = new ServiceAccountBuilder()
                .withMetadata(new ObjectMetaBuilder().withName("core-controller").build());
        client.inNamespace(namespace).serviceAccounts().createOrReplace(saBuilder.build());
        RoleBinding roleBinding = generateRoleBinding(namespace, namespace);
        client.inNamespace(namespace).rbac().roleBindings().createOrReplace(roleBinding);
    }

    public RoleBinding generateRoleBinding(String namespace, String watchedNamespace) {
        Subject ks = new SubjectBuilder()
                .withKind("ServiceAccount")
                .withName("core-controller")
                .withNamespace(namespace)
                .build();

        RoleRef roleRef = new RoleRefBuilder()
                .withName("core-controller")
                .withApiGroup("rbac.authorization.k8s.io")
                .withKind("ClusterRole")
                .build();

        RoleBinding rb = new RoleBindingBuilder()
                .withNewMetadata()
                .withName("core-controller")
                .withNamespace(watchedNamespace)
//                .withOwnerReferences(createOwnerReference())
//                .withLabels(labels.toMap())
                .endMetadata()
                .withRoleRef(roleRef)
                .withSubjects(ks)
                .build();

        return rb;
    }

    public void deploy(String env, WebsiteConfig config) {
        String namespace = getNameSpaceName(env);
        log.infof("Deploying website config, env=%s namespace=%s", env, namespace);

        staticContentController.updateConfigSecret(env, namespace, config);
        staticContentController.deploy(namespace);
        // TODO: Wait till deployment is ready

        gatewayController.updateConfigSecret(env, namespace, config);
        gatewayController.deploy(namespace);
    }

    public void redeploy(String env, WebsiteConfig config) {
        log.infof("Redeploying website config, env=%s", env);
        String namespace = getNameSpaceName(env);
        staticContentController.updateConfigSecret(env, namespace, config);
        staticContentController.redeploy(namespace);
        // TODO: Wait till deployment is ready

        gatewayController.updateConfigSecret(env, namespace, config);
        gatewayController.redeploy(namespace);
    }
}
