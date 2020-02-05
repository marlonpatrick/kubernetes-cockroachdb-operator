#!/bin/bash
set -eo pipefail

list_databases(){

	if [ "$COCKROACHDB_BACKUP_DATABASES" =  "*" ]; then

		./cockroach sql --insecure --host=$cockroachdb_backup_host -e "show databases" \
			| tail -n +2 | grep -v -E 'defaultdb|postgres|system'

		return
	fi;

 	echo $COCKROACHDB_BACKUP_DATABASES | tr ";" "\n"
}

select_cockroachdb_current_time(){
	local cockroachdb_current_time=$(./cockroach sql --insecure --host=$cockroachdb_backup_host -e "select now() at time zone 'GMT' as current_time")
	cockroachdb_current_time=${cockroachdb_current_time/current_time/}

	# 2020-02-05 01:40:18.738281+00:00
	echo $cockroachdb_current_time
}

generate_backup_file_name(){

	# 2020-02-05 01:40:18.738281+00:00
	local database_current_time=$1

	# 2020-02-05 01:40:18.738281+00:00 -->> 2020-02-05_01-40-18-738281_00-00
	echo $database_current_time | sed -r 's/:/-/g' | sed -r 's/\./-/g' | sed -r 's/\+/_/g' | sed -r 's/\s/_/g'
}

dump_database(){
	local host=$1

	local database=$2

	local cockroachdb_current_time=$(select_cockroachdb_current_time)

	local backup_file_name=$(generate_backup_file_name "$cockroachdb_current_time")

	mkdir $database

	./cockroach dump $database --insecure --host=$host \
		--as-of="$cockroachdb_current_time" > $database/$backup_file_name.sql

	gzip $database/$backup_file_name.sql

	echo $backup_file_name.sql.gz
}

upload_aws_s3(){
	local database=$1

	local file_name=$2

	aws s3 cp $database/$file_name s3://$COCKROACHDB_BACKUP_AWS_S3_BUCKET/$COCKROACHDB_BACKUP_AWS_S3_ROOT_PATH/$database/
}

remove_old_backup_files_aws_s3(){
	local database=$1

	for old_backup_file in $(aws s3 ls s3://$COCKROACHDB_BACKUP_AWS_S3_BUCKET/$COCKROACHDB_BACKUP_AWS_S3_ROOT_PATH/$database/ | sort -k 4 -r | tail -n +$(($COCKROACHDB_BACKUP_MAX_KEPT_BACKUPS+1)) ); do
		aws s3 rm s3://$COCKROACHDB_BACKUP_AWS_S3_BUCKET/$COCKROACHDB_BACKUP_AWS_S3_ROOT_PATH/$database/$old_backup_file
	done
}


#############
#COCKROACHDB_BACKUP_CLUSTER_NAME=example-cockroachdb-cluster
#COCKROACHDB_BACKUP_DATABASES="*"
#COCKROACHDB_BACKUP_AWS_S3_BUCKET=mpbs-cockroachdb-operator-test
#COCKROACHDB_BACKUP_MAX_KEPT_BACKUPS=5
#############

echo "=========================================================================="
echo "BACKUP PARAMETERS"
echo "COCKROACHDB_BACKUP_CLUSTER_NAME: $COCKROACHDB_BACKUP_CLUSTER_NAME"
echo "COCKROACHDB_BACKUP_DATABASES: $COCKROACHDB_BACKUP_DATABASES"
echo "COCKROACHDB_BACKUP_AWS_S3_BUCKET: $COCKROACHDB_BACKUP_AWS_S3_BUCKET"
echo "COCKROACHDB_BACKUP_AWS_S3_ROOT_PATH: $COCKROACHDB_BACKUP_AWS_S3_ROOT_PATH"
echo "COCKROACHDB_BACKUP_MAX_KEPT_BACKUPS: $COCKROACHDB_BACKUP_MAX_KEPT_BACKUPS"
echo "=========================================================================="
printf "\n\n"

cockroachdb_backup_host=$COCKROACHDB_BACKUP_CLUSTER_NAME-public

cd /cockroach

for current_database in $(list_databases); do

	echo "Starting backup for database $current_database at $(date +"%F %T")..."

	echo "Starting dump at $(date +"%F %T")..."

	dump_file_name=$(dump_database $cockroachdb_backup_host $current_database)

	echo "Starting upload to AWS S3 at $(date +"%F %T")..."

	upload_aws_s3 $current_database $dump_file_name

	echo "Starting remove old backups from AWS S3 at $(date +"%F %T")..."

	remove_old_backup_files_aws_s3 $current_database

	echo "Backup for database $current_database finished at $(date +"%F %T")"

	printf "\n"
done
