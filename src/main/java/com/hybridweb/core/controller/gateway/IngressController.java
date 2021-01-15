package com.hybridweb.core.controller.gateway;

import com.hybridweb.core.controller.Utils;
import com.hybridweb.core.controller.website.model.ComponentConfig;
import com.hybridweb.core.controller.website.model.ComponentSpec;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.networking.v1beta1.*;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class IngressController {

    private static final Logger log = Logger.getLogger(IngressController.class);

    final String INGRESS_NAME = "hybridweb";

    @Inject
    DefaultOpenShiftClient client;

    @ConfigProperty(name = "app.staticcontent.url")
    protected String staticContentUrl;

    @ConfigProperty(name = "app.staticcontent.rootcontext")
    protected String rootContext;

    @ConfigProperty(name = "app.controller.website.domain")
    protected String domain;

    public void updateIngress(String targetEnv, String namespace, WebsiteConfig config) throws MalformedURLException {
        List<HTTPIngressPath> paths = new ArrayList<>();
        for (ComponentConfig c : config.getComponents()) {
            ComponentSpec spec = c.getSpec();
            if (!Utils.isEnvIncluded(spec.getEnvs(), targetEnv)) {
                continue;
            }
            String routeContext = c.getContext();
            String url;
            if (c.isKindGit()) {
                url = staticContentUrl;
            } else if (c.isKindService()) {
                url = c.getSpec().getUrl();
            } else {
                throw new RuntimeException("Unknown kind: " + c.getKind());
            }

            URL urlObj = new URL(url);
            IngressBackend backend = new IngressBackend();
            backend.setServiceName(urlObj.getHost());
            backend.setServicePort(new IntOrString(urlObj.getPort()));
            HTTPIngressPath path = new HTTPIngressPath();
            path.setPath(routeContext);
            path.setPathType("Prefix");
            path.setBackend(backend);
            paths.add(path);
        }
        IngressRule rule = new IngressRuleBuilder().withHost(domain).withNewHttp().withPaths(paths).endHttp().build();

        Map<String, String> annotations = new LinkedHashMap<>();
//        annotations.put("nginx.ingress.kubernetes.io/rewrite-target", "/$1");
        IngressBuilder builder = new IngressBuilder().withMetadata(new ObjectMetaBuilder().withName(INGRESS_NAME)
                .withAnnotations(annotations).build())
                .withSpec(new IngressSpecBuilder().withRules(rule).build());

        Ingress ingress = builder.build();
        log.infof("Ingress: %s", ingress);

        client.inNamespace(namespace).network().ingress().createOrReplace(ingress);
    }

}
