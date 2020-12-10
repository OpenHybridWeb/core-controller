package com.hybridweb.core.controller;

import com.hybridweb.core.controller.gateway.GatewayController;
import com.hybridweb.core.controller.staticcontent.StaticContentController;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class MainController {

    private static final Logger log = Logger.getLogger(MainController.class);


    @Inject
    GatewayController gatewayController;

    @Inject
    StaticContentController staticContentController;


    @Inject
    KubernetesClient client;

    public void createNamespaces(List<String> envs) {

    }

    public void deploy(String env, WebsiteConfig config) {
        staticContentController.updateConfigSecret(env, config);
        staticContentController.deploy();
        // TODO: Wait till deployment is ready

        gatewayController.updateConfigSecret(env, config);
        gatewayController.deploy();
    }

    public void redeploy(String env, WebsiteConfig config) {
        log.info("Redeploying website config");
        staticContentController.updateConfigSecret(env, config);
        staticContentController.redeploy();
        // TODO: Wait till deployment is ready

        gatewayController.updateConfigSecret(env, config);
        gatewayController.redeploy();
    }
}
