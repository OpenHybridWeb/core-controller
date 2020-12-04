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
