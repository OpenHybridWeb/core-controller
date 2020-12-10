package com.hybridweb.core.controller;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class KubernetesClientProducer {

    @Produces
    public DefaultKubernetesClient kubernetesClient() {
        return new DefaultKubernetesClient();
    }
}
