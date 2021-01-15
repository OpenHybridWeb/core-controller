package com.hybridweb.core.controller.gateway;

import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.api.model.RouteSpecBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RouterController {

    private static final Logger log = Logger.getLogger(RouterController.class);

    @Inject
    private DefaultOpenShiftClient client;

    public void updateWebsiteRoutes(String targetEnv, String namespace, WebsiteConfig config) {
        for (ComponentConfig component : config.getComponents()) {
            String context = component.getContext();

            RouteSpecBuilder spec = new RouteSpecBuilder();

            String name = "website-" + context + "-" + targetEnv;
            RouteBuilder builder = new RouteBuilder()
                    .withMetadata(new ObjectMetaBuilder().withName(name).build())
                    .withSpec(spec.build());

            Route route = builder.build();
            log.infof("route=%s", route);

            client.inNamespace(namespace).routes().createOrReplace(route);
        }
    }

}
