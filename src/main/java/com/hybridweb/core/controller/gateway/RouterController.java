package com.hybridweb.core.controller.gateway;

import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.openshift.api.model.*;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RouterController {

    private static final Logger log = Logger.getLogger(RouterController.class);

    @Inject
    private DefaultOpenShiftClient client;

    @ConfigProperty(name = "app.controller.website.domain")
    protected String domain;

    public void updateWebsiteRoutes(String targetEnv, String namespace, WebsiteConfig config) {
        final String hostSuffix = "-" + namespace + "." + domain;
        // TODO: It's not needed to create all routes for sub pathes when root path is present
        for (ComponentConfig component : config.getComponents()) {
            String context = component.getContext();
            String sanityContext = context.replace("/", "").replace("_", "");

            RouteTargetReferenceBuilder targetReference = new RouteTargetReferenceBuilder().withKind("Service").withWeight(100);
            RoutePortBuilder routePortBuilder = new RoutePortBuilder();
            if (component.isKindGit()) {
                targetReference.withName("static-" + targetEnv);
                routePortBuilder.withTargetPort(new IntOrString(8080));
            } else {
                targetReference.withName(component.getSpec().getServiceName());
                routePortBuilder.withTargetPort(new IntOrString(component.getSpec().getTargetPort()));
            }

            String host = "web-" + targetEnv + hostSuffix;
            RouteSpecBuilder spec = new RouteSpecBuilder()
                    .withHost(host)
                    .withPath(context)
                    .withTo(targetReference.build())
                    .withPort(routePortBuilder.build())
                    .withTls(new TLSConfigBuilder().withNewTermination("edge").build());

            String name = getRouteName(sanityContext, targetEnv);
            RouteBuilder builder = new RouteBuilder()
                    .withMetadata(new ObjectMetaBuilder().withName(name).build())
                    .withSpec(spec.build());

            Route route = builder.build();
            log.infof("route=%s", route);

            client.inNamespace(namespace).routes().createOrReplace(route);
        }
    }

    public String getRouteName(String sanityContext, String env) {
        StringBuilder routeName = new StringBuilder("static-");
        if (StringUtils.isEmpty(sanityContext)) {
            routeName.append("root");
        } else {
            routeName.append(sanityContext);
        }
        routeName.append("-" + env);

        return routeName.toString();
    }

}
