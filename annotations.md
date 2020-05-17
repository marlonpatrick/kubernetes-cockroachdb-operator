##### Issues abertas

	- https://github.com/fabric8io/kubernetes-client/issues/1979

	- https://github.com/jvm-operators/operator-mvn-archetypes/issues/5

--------------------------------------------------------------------

# DUMP
kubectl -n cluster01 run cockroachdb -it --image=cockroachdb/cockroach:v19.2.2 --rm --restart=Never -- dump  --as-of='-30s' bank --insecure --host=example-cockroachdb-cluster-public > cockroachdb-bank-dump.sql


# RESTORE
cockroach sql --insecure --database=startrek --user=maxroach < backup.sql

--------------------------------------------------------------------


kubectl -n cluster02 run cockroachdb -it \
--image=cockroachdb/cockroach:v19.2.2 \
--rm \
--restart=Never \
-- sql \
--insecure \
--host=example-cockroachdb-cluster-public


CREATE DATABASE test1;

CREATE TABLE test1.accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      balance DECIMAL
  );

INSERT INTO test1.accounts (balance)
  VALUES
      (1000.50), (20000), (380), (500), (55000);

SELECT * FROM test1.accounts;

--------------------------------------------------------------------

make backupper-image-build backupper-push-image

make build push-image deploy-dev

kc -n simple-cockroachdb-operator logs -f deployment.apps/simple-cockroachdb-operator
