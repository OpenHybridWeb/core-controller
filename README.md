# core-controller
Core component to control gateway and staticcontent containers.

Env variable pointing to git repo is `APP_CONTROLLER_WEBSITE_URL`.
Default value is `https://github.com/OpenHybridWeb/example-staticweb.git`

## How to run

### Minikube

Start minikube
```shell
minikube start
```

Service account and Deploy
```shell
kubectl apply -f src/main/k8s/service-account.yaml
kubectl apply -f src/main/k8s/core-controller.yaml
```

You're done. Expose the gateway via
```shell
minikube service core-gateway
```
and hit 

See the result in dashboard

```shell
minikube dashboard
```


#### Local Development

To run as locally do not deploy docker image via `kubectl apply -f src/main/k8s/core-controller.yaml`
and just expose minikube api to port 8090
```shell
kubectl proxy --port=8090
```

Run controller on your JVM

```shell
mvn package
mvn quarkus:dev
# or mvn -jar target/controller-1.0.0-SNAPSHOT-runner.jar
```

#### Cleanup

```shell
minikube stop
minikube delete
```