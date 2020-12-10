package com.hybridweb.core.controller;

import com.hybridweb.core.controller.website.model.WebsiteDefaults;

import java.util.List;
import java.util.Map;

public class Utils {

    public static boolean isEnvEnabled(WebsiteDefaults defaults, String targetEnv) {
        return defaults.getEnvs().contains(targetEnv);
    }

    /**
     * Check if envrionment is included in "spec.envs" array.
     *
     * @param envs
     * @param targetEnv
     * @return
     */
    public static boolean isEnvIncluded(List<Map<String, Object>> envs, String targetEnv) {
        if (envs == null) {
            return true;
        }
        for (Map<String, Object> env : envs) {
            if (env.containsKey(targetEnv)) {
                return true;
            }
        }
        return false;
    }


}