IMAGE?=quay.io/marlonpatrick/simple-cockroachdb-operator
NS1?=cluster01
NS2?=cluster02

.PHONY: build
build: package image-build

.PHONY: package
package:
	MAVEN_OPTS="-Djansi.passthrough=true -Dplexus.logger.type=ansi $(MAVEN_OPTS)" sh ./mvnw clean package -DskipTests

.PHONY: image-build
image-build:
	podman build -t $(IMAGE):latest -f Dockerfile .

.PHONY: push-image
push-image:
	podman push $(IMAGE):latest
	
.PHONY: deploy-dev
deploy-dev:
	kubectl config use-context minikube
	kubectl delete namespace $(NS1)
	kubectl create namespace $(NS1)
	kubectl delete namespace $(NS2)
	kubectl create namespace $(NS2)
	kubectl delete -f operator-deploy/operator.yaml
	kubectl delete crd/cockroachdbclusters.io.marlonpatrick
	kubectl apply -f operator-deploy/operator.yaml --wait=true
	sleep 10
	kubectl apply -f operator-deploy/example-cockroachdb.yaml -n $(NS1)
	kubectl apply -f operator-deploy/example-cockroachdb.yaml -n $(NS2)

.PHONY: test
test:
	MAVEN_OPTS="-Djansi.passthrough=true -Dplexus.logger.type=ansi $(MAVEN_OPTS)" sh ./mvnw clean test
