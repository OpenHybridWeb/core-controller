# core-controller
Core component to control gateway and staticcontent containers.

Env variable pointing to git repo is `APP_CONTROLLER_WEBSITE_URL`.
Default value is `https://github.com/OpenHybridWeb/example-staticweb.git`

## REST API

* /health/live
* /health/ready
* /_controller/api/github/website
* /_controller/api/github/component/{name}


## How to run

### Minikube

Start minikube
```shell
minikube start
```

Namespace
```shell
kubectl create namespace web-dev
```

Service account, configmap and Deploy
```shell
kubectl -n web-dev apply -f src/main/k8s/service-account.yaml
kubectl -n web-dev create configmap core-controller-config --from-literal=APP_CONTROLLER_ENV=dev --from-literal=APP_CONTROLLER_WEBSITE_URL=https://github.com/OpenHybridWeb/example-staticweb.git
kubectl -n web-dev apply -f src/main/k8s/core-controller.yaml
```

You're done. Expose the gateway via
```shell
minikube -n web-dev service core-gateway
```

See the result in dashboard

```shell
minikube dashboard
```


### Local Development

Just create a namespace and expose minikube api to port 8090
```shell
kubectl create namespace web-dev
kubectl proxy --port=8090
```

Run controller on your JVM which by defaults set 
```
app.controller.website.url=https://github.com/OpenHybridWeb/example-staticweb.git
app.controller.env=dev
```

```shell
mvn compile
mvn quarkus:dev
```

To deploy the controller as "operator" which creates appropriate namespaces and deploy in each of them configured controller

```shell
mvn clean package
APP_CONTROLLER_WEBSITE_URL=https://github.com/OpenHybridWeb/example-staticweb.git java -jar target/controller-1.0.0-SNAPSHOT-runner.jar
```

#### Cleanup

```shell
minikube stop
minikube delete
```
