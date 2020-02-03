# simple-cockroachdb-operator

# Very Quick Start

Run the `simple-cockroachdb-operator` deployment:

```bash
kubectl apply --wait=true -f https://raw.githubusercontent.com/marlonpatrick/kubernetes-cockroachdb-operator/master/operator-deploy/operator.yaml

kubectl -n simple-cockroachdb-operator get all
```

Create a `CockroachDBCluster` resource from the prepared example:

```bash
kubectl -n my-namespace apply --wait=true -f https://raw.githubusercontent.com/marlonpatrick/kubernetes-cockroachdb-operator/master/operator-deploy/example-cockroachdb.yaml

kubectl -n my-namespace get all
```

Test the cluster deploy

```bash
kubectl -n my-namespace run cockroachdb -it \
--image=cockroachdb/cockroach:v19.2.2 \
--rm \
--restart=Never \
-- sql \
--insecure \
--host=example-cockroachdb-cluster-public
```

```sql
CREATE DATABASE bank;

CREATE TABLE bank.accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      balance DECIMAL
  );

INSERT INTO bank.accounts (balance)
  VALUES
      (1000.50), (20000), (380), (500), (55000);

SELECT * FROM bank.accounts;
```
