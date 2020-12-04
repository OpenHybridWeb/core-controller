package com.hybridweb.core.controller.website;

import com.hybridweb.core.controller.gateway.GatewayController;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ApplicationScoped
public class WebsiteConfigService {

    private static final Logger log = Logger.getLogger(WebsiteConfigService.class);

    @ConfigProperty(name = "app.controller.website.url")
    String gitUrl;

    String workDir = System.getProperty("user.dir");

    String configPath = "/.openhybridweb/website.yaml";

    WebsiteConfig config;

    @Inject
    GatewayController gatewayController;

    void onStart(@Observes StartupEvent ev) throws GitAPIException, IOException {
        log.info("Initializing website config");
        File gitDir = new File(workDir + "/website.git");
        if (!gitDir.exists()) {
            try (Git git = Git.cloneRepository().setURI(gitUrl).setDirectory(gitDir).call()) {
                String lastCommitMessage = git.log().call().iterator().next().getShortMessage();
                log.infof("Website config cloned to dir=%s commit_message='%s'", gitDir, lastCommitMessage);
            }
        } else {
            log.infof("Website config already cloned. skipping dir=%s", gitDir);
        }
        try (InputStream is = new FileInputStream(gitDir.getAbsolutePath() + configPath)) {
            config = loadYaml(is);
        }
        gatewayController.updateConfigSecret(config);
        gatewayController.deploy();
    }

    public static WebsiteConfig loadYaml(InputStream is) {
        Yaml yaml = new Yaml(new Constructor(WebsiteConfig.class));
        WebsiteConfig c = yaml.load(is);
        log.infof("Loaded website.yaml content:\n%s", yaml.dumpAsMap(c));
        return c;
    }


}
