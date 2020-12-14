# core-controller
Core component to control gateway and staticcontent containers.

Env variable pointing to git repo is `APP_CONTROLLER_WEBSITE_URL`.

## REST API

* /health/live
* /health/ready
* /_controller/api/webhook/website
* /_controller/api/webhook/component/{name}
* /_controller/api/staticcontent/components
* /_controller/api/staticcontent/update/{name}


## How to run

### Minikube

Start minikube
```shell
minikube start
```

Namespace
```shell
kubectl create namespace dev
```

Service account, configmap and Deploy
```shell
kubectl -n dev apply -f src/main/k8s/service-account.yaml
kubectl -n dev create configmap core-controller-config --from-literal=APP_CONTROLLER_ENV=dev --from-literal=APP_CONTROLLER_WEBSITE_URL=https://github.com/OpenHybridWeb/example-websites.git --from-literal=APP_CONTROLLER_WEBSITE_CONFIG_DIR=static
kubectl -n dev apply -f src/main/k8s/core-controller.yaml
```

You're done. Expose the gateway via
```shell
minikube -n dev service core-gateway
```

See the result in dashboard

```shell
minikube dashboard
```


### Local Development

Just create a namespace and expose minikube api to port 8090
```shell
kubectl create namespace dev
kubectl proxy --port=8090
```

Run controller on your JVM which by defaults set 
```
app.controller.website.url=https://github.com/OpenHybridWeb/example-websites.git
app.controller.website.config.dir=static-restapi
app.controller.env=dev
```

```shell
mvn compile
mvn quarkus:dev
```

To deploy the controller as "operator" which creates appropriate namespaces and deploy in each of them configured controller

```shell
kubectl -n default apply -f src/main/k8s/service-account.yaml

mvn clean package
APP_CONTROLLER_WEBSITE_URL=https://github.com/OpenHybridWeb/example-websites.git APP_CONTROLLER_WEBSITE_CONFIG_DIR=static-restapi java -jar target/controller-1.0.0-SNAPSHOT-runner.jar
```

#### Cleanup

```shell
minikube stop
minikube delete
```
