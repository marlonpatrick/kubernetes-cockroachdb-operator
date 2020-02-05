OPERATOR_IMAGE?=quay.io/marlonpatrick/simple-cockroachdb-operator
BACKUPPER_IMAGE?=quay.io/marlonpatrick/simple-cockroachdb-backupper
NS1?=cluster01
NS2?=cluster02

.PHONY: build
build: package image-build

.PHONY: package
package:
	MAVEN_OPTS="-Djansi.passthrough=true -Dplexus.logger.type=ansi $(MAVEN_OPTS)" sh ./mvnw clean package -DskipTests

.PHONY: image-build
image-build:
	podman build -t $(OPERATOR_IMAGE):latest -f Dockerfile.operator .

.PHONY: push-image
push-image:
	podman push $(OPERATOR_IMAGE):latest
	
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
	kubectl -n $(NS2) create secret generic aws-settings --from-file=backupper-image/aws-dev-settings/credentials --from-file=backupper-image/aws-dev-settings/config		
	sleep 10
	kubectl apply -f operator-deploy/example-cockroachdb.yaml -n $(NS1)
	kubectl apply -f operator-deploy/example-cockroachdb-backup.yaml -n $(NS2)

.PHONY: backupper-image-build
backupper-image-build:
	podman build -t $(BACKUPPER_IMAGE):latest -f backupper-image/Dockerfile.backupper backupper-image/

.PHONY: backupper-push-image
backupper-push-image:
	podman push $(BACKUPPER_IMAGE):latest

.PHONY: test
test:
	MAVEN_OPTS="-Djansi.passthrough=true -Dplexus.logger.type=ansi $(MAVEN_OPTS)" sh ./mvnw clean test