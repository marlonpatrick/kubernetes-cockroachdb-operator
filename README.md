# simple-cockroachdb-operator

# Quick Start

1 - Run the `simple-cockroachdb-operator` deployment:

```bash
kubectl apply --wait=true -f https://raw.githubusercontent.com/marlonpatrick/kubernetes-cockroachdb-operator/master/operator-deploy/operator.yaml

kubectl -n simple-cockroachdb-operator get all
```

2 - Create a namespace to deploy a example cluster:

```bash
kubectl create namespace test-cockroachdb-operator
```

3 - Create a simple `CockroachDBCluster` without backup activated OR create a cluster with backup:

3.1 - Without backup:

```bash
kubectl -n test-cockroachdb-operator apply --wait=true -f https://raw.githubusercontent.com/marlonpatrick/kubernetes-cockroachdb-operator/master/operator-deploy/example-cockroachdb.yaml

# Wait until pod example-cockroachdb-cluster-0 is Ready/Running
watch kubectl -n test-cockroachdb-operator get all
```

3.2 With backup:

Setup AWS Secret that contains AWS config/credentials files (only necessary with example-cockroachdb-with-backup.yaml):

```bash
$ cat $AWS_DIR/credentials
[default]
aws_access_key_id = XXX
aws_secret_access_key = XXX

$ cat $AWS_DIR/config
[default]
region = <region>

kubectl -n test-cockroachdb-operator create secret generic aws-settings --from-file=$AWS_DIR/credentials --from-file=$AWS_DIR/config
```

Create the cluster:

```bash
# Download example file
wget https://raw.githubusercontent.com/marlonpatrick/kubernetes-cockroachdb-operator/master/operator-deploy/example-cockroachdb-with-backup.yaml

# MY_BUCKET: your AWS S3 bucket where backup files will be placed.
# MY_BUCKET_ROOT_PATH: the root directory from your AWS S3 Bucket where backup files will be placed. 
# Final backup path: BUCKET/ROOT_PATH/database-name/backup-file.sql.gz
sed -e 's|${MY_BUCKET}|YOUR_BUCKET_NAME|g' \
	-e 's|${MY_BUCKET_ROOT_PATH}|YOUR_BUCKET_ROOT_PATH|g' \
	example-cockroachdb-with-backup.yaml \
	| kubectl -n test-cockroachdb-operator apply --wait=true -f -

# Wait until pod example-cockroachdb-cluster-0 is Ready/Running
watch kubectl -n test-cockroachdb-operator get all
```

4 - Test the cluster deploy:

```bash
kubectl -n test-cockroachdb-operator run cockroachdb -it \
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

5 - After 3 minutes, check your AWS S3 Bucket 