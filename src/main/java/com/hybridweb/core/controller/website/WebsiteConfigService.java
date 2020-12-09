package com.hybridweb.core.controller.website;

import com.hybridweb.core.controller.gateway.GatewayController;
import com.hybridweb.core.controller.staticcontent.StaticContentController;
import com.hybridweb.core.controller.website.model.WebsiteConfig;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
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

    @ConfigProperty(name = "app.controller.env")
    protected String env;

    String workDir = System.getProperty("user.dir");

    String configPath = "/.openhybridweb/website.yaml";

    WebsiteConfig config;

    @Inject
    GatewayController gatewayController;

    @Inject
    StaticContentController staticContentController;


    void onStart(@Observes StartupEvent ev) throws GitAPIException, IOException {
        log.info("Initializing website config");
        File gitDir = getGitDir();
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
        staticContentController.updateConfigSecret(env, config);
        staticContentController.deploy();
        // TODO: Wait till deployment is ready

        gatewayController.updateConfigSecret(env, config);
        gatewayController.deploy();
    }

    public File getGitDir() {
        return new File(workDir + "/website.git");
    }

    public static WebsiteConfig loadYaml(InputStream is) {
        Yaml yaml = new Yaml(new Constructor(WebsiteConfig.class));
        WebsiteConfig c = yaml.load(is);
        log.infof("Loaded website.yaml content:\n%s", yaml.dumpAsMap(c));
        return c;
    }

    public void redeploy() throws GitAPIException, IOException {
        log.info("Redeploying website config");
        File gitDir = getGitDir();
        PullResult pullResult = Git.open(gitDir).pull().call();
        if (!pullResult.isSuccessful()) {
            throw new RuntimeException("Cannot pull repo. result=" + pullResult);
        }
        log.infof("Website config pulled in dir=%s commit_message='%s'", gitDir, pullResult.getFetchResult().getMessages());

        try (InputStream is = new FileInputStream(gitDir.getAbsolutePath() + configPath)) {
            config = loadYaml(is);
        }
        staticContentController.updateConfigSecret(env, config);
        staticContentController.redeploy();
        // TODO: Wait till deployment is ready

        gatewayController.updateConfigSecret(env, config);
        gatewayController.redeploy();
    }

}
