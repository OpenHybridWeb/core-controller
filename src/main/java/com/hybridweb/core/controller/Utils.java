package com.hybridweb.core.controller;

import com.hybridweb.core.controller.website.model.WebsiteConfig;

import java.util.Map;

public class Utils {

    public static boolean isEnvEnabled(WebsiteConfig config, String targetEnv) {
        return config.getEnvs().containsKey(targetEnv);
    }

    /**
     * Check if environment is included in "spec.envs" array.
     *
     * @param envs
     * @param targetEnv
     * @return
     */
    public static boolean isEnvIncluded(Map<String, Map<String, Object>> envs, String targetEnv) {
        if (envs == null) {
            return true;
        }
        return envs.containsKey(targetEnv);
    }


}
