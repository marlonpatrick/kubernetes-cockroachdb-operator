package io.marlonpatrick.cockroachdb.operator;

import io.fabric8.kubernetes.api.model.batch.CronJob;

interface CockroachDBBackupStorage {
	
	void configure(CronJob cronJobBackup);
}
