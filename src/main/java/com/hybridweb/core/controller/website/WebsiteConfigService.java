package com.hybridweb.core.controller.website;

import com.hybridweb.core.controller.MainController;
import com.hybridweb.core.controller.website.model.Environment;
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
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class WebsiteConfigService {

    private static final Logger log = Logger.getLogger(WebsiteConfigService.class);

    @ConfigProperty(name = "app.controller.website.url")
    String gitUrl;

    @ConfigProperty(name = "app.controller.namespace")
    protected Optional<String> namespace;

    String workDir = System.getProperty("user.dir");

    @ConfigProperty(name = "app.controller.website.config.dir")
    String configDir;
    @ConfigProperty(name = "app.controller.website.config.filename")
    String configFilename;

    WebsiteConfig config;

    @Inject
    MainController mainController;

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
        try (InputStream is = new FileInputStream(getWebsiteConfigPath(gitDir.getAbsolutePath()))) {
            config = loadYaml(is);
        }

        Map<String, Environment> envs = config.getEnvs();
        for (Map.Entry<String, Environment> envEntry : envs.entrySet()) {
            if (!namespace.isEmpty() && !envEntry.getValue().getNamespace().equals(namespace.get())) {
                log.infof("namespace ignored name=%s", namespace);
                continue;
            }
            // ? create namespace ???
            mainController.setupCoreServices(envEntry.getKey(), config);
        }
    }

    public String getWebsiteConfigPath(String baseDir) {
        return baseDir + "/" + configDir + "/" + configFilename;
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

    public void reload() throws GitAPIException, IOException {
        File gitDir = getGitDir();
        PullResult pullResult = Git.open(gitDir).pull().call();
        if (!pullResult.isSuccessful()) {
            throw new RuntimeException("Cannot pull repo. result=" + pullResult);
        }
        log.infof("Website config pulled in dir=%s commit_message='%s'", gitDir, pullResult.getFetchResult().getMessages());

        try (InputStream is = new FileInputStream(getWebsiteConfigPath(gitDir.getAbsolutePath()))) {
            config = WebsiteConfigService.loadYaml(is);
        }

        // TODO: Check if any change happens. If not skip redeploy

        Map<String, Environment> envs = config.getEnvs();
        for (Map.Entry<String, Environment> envEntry : envs.entrySet()) {
            if (!namespace.isEmpty() && !envEntry.getValue().getNamespace().equals(namespace.get())) {
                log.infof("namespace ignored name=%s", namespace);
                continue;
            }
            mainController.redeploy(envEntry.getKey(), config);
        }
    }
}