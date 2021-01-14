package com.hybridweb.core.controller;

import com.hybridweb.core.controller.gateway.IngressController;
import com.hybridweb.core.controller.staticcontent.StaticContentController;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.rbac.*;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MainController {

    private static final Logger log = Logger.getLogger(MainController.class);

    @ConfigProperty(name = "app.controller.namespace.prefix")
    Optional<String> namespacePrefix;

    @Inject
    StaticContentController staticContentController;

    @Inject
    IngressController ingressController;

    @Inject
    DefaultKubernetesClient client;

    String nameSpaceLabelValue = "openhybridweb";

    public void createNamespaces(String prefix, List<String> envs) {
        for (String env : envs) {
            String name = getNameSpaceName(prefix, env);
            Namespace ns = new NamespaceBuilder().withNewMetadata().withName(name).addToLabels("app", nameSpaceLabelValue).endMetadata().build();
            client.namespaces().withName(name).createOrReplace(ns);
            log.infof("Namespace created. name=%s", name);
        }
    }

    public String getNameSpaceName(String namespacePrefix, String env) {
        return namespacePrefix + env;
    }

    public void setupCoreServices(String env, WebsiteConfig config) throws MalformedURLException {
        String namespace = getNameSpaceName(config.getDefaults().getNamespacePrefix(), env);
        staticContentController.updateConfigSecret(env, namespace, config);
        staticContentController.deploy(namespace);

//        ingressController.updateIngress(env, namespace, config);
    }

    public void updateServiceAccount(String namespace) {
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

    public void redeploy(String env, WebsiteConfig config) throws MalformedURLException {
        log.infof("Redeploying website config, env=%s", env);
        String namespace = getNameSpaceName(config.getDefaults().getNamespacePrefix(), env);
        staticContentController.updateConfigSecret(env, namespace, config);
        staticContentController.redeploy(namespace);
        // TODO: Wait till deployment is ready

//        ingressController.updateIngress(env, namespace, config);
    }
}
