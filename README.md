# simple-cockroachdb-operator

# Very Quick Start

Run the `simple-cockroachdb-operator` deployment:

```bash
kubectl apply -f operator-deploy/operator.yaml --wait=true
```

Create a `CockroachDBCluster` resource from the prepared example:

```bash
kubectl apply -f operator-deploy/example-cockroachdb.yaml -n my-namespace

watch kubectl -n my-namespace get all
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
