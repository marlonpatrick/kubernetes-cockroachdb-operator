package io.marlonpatrick.cockroachdb.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;

class CockroachDBClusterDeployer {

	private static final Logger log = LoggerFactory.getLogger(CockroachDBClusterDeployer.class.getName());

	private static final String CLUSTER_NAME_PARAM_KEY = "cockroachdbcluster.name";
	private static final String CLUSTER_UID_PARAM_KEY = "cockroachdbcluster.uid";
	private static final String CLUSTER_STORAGE_PARAM_KEY = "cockroachdbcluster.storage";

	private static final String BACKUP_DATABASE_PARAM_KEY = "cockroachdbcluster.backup.databases";
	private static final String BACKUP_CRON_SCHEDULE_PARAM_KEY = "cockroachdbcluster.backup.cronSchedule";
	private static final String BACKUP_MAX_KEPT_PARAM_KEY = "cockroachdbcluster.backup.maxKeptBackups";
	private static final String BACKUP_S3_BUCKET_PARAM_KEY = "cockroachdbcluster.backup.storage.s3.bucket";
	private static final String BACKUP_S3_ROOT_PATH_PARAM_KEY = "cockroachdbcluster.backup.storage.s3.rootPath";
	private static final String BACKUP_S3_SETTINGS_SECRET_PARAM_KEY = "cockroachdbcluster.backup.storage.s3.awsSettingsSecret";

	
	private static final String CLUSTER_CLUSTER_YAML_FILE_PATH = "/cockroachdb-deploy/cockroachdb-cluster.yaml";
	private static final String CLUSTER_JOB_INIT_YAML_FILE_PATH = "/cockroachdb-deploy/cockroachdb-cluster-init.yaml";
	private static final String CLUSTER_CRONJOB_BACKUP_YAML_FILE_PATH = "/cockroachdb-deploy/cockroachdb-backup.yaml";

	private KubernetesClient client;

    CockroachDBClusterDeployer(KubernetesClient client) {
        this.client = client;
    }

    void deploy(CockroachDBCluster cluster) {

		log.info("Deploying cluster {} in namespace {}.", cluster.getName(), cluster.getNamespace());

    	Map<String, String> parameters = parameters(cluster);

		List<HasMetadata> resourceList = new ArrayList<HasMetadata>();
		resourceList.addAll(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_CLUSTER_YAML_FILE_PATH), parameters));
		resourceList.add(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_JOB_INIT_YAML_FILE_PATH), parameters));
		
    	if(cluster.getBackup() !=null) {
    		resourceList.add(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_CRONJOB_BACKUP_YAML_FILE_PATH), parameters));
    	}

		log.debug("deploy: parameters: {}", parameters);
		log.debug("deploy: cluster: {}", cluster);
		log.debug("deploy: resourceList: {}", resourceList);

        client.resourceList(resourceList).inNamespace(cluster.getNamespace()).createOrReplace();

		log.info("Cluster {} for namespace {} deployed.", cluster.getName(), cluster.getNamespace());
    }
    
    void undeploy(CockroachDBCluster cluster) {
		log.info("Undeploying cluster {} in namespace {}.", cluster.getName(), cluster.getNamespace());
		
    	Map<String, String> parameters = parameters(cluster);

		List<HasMetadata> resourceList = new ArrayList<HasMetadata>();
		resourceList.addAll(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_CLUSTER_YAML_FILE_PATH), parameters));
		
    	if(cluster.getBackup() !=null) {
    		resourceList.addAll(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_JOB_INIT_YAML_FILE_PATH), parameters));
    	}

		log.debug("undeploy: parameters: {}", parameters);
		log.debug("undeploy: cluster: {}", cluster);
		log.debug("undeploy: resourceList: {}", resourceList);

        client.resourceList(resourceList).inNamespace(cluster.getNamespace()).delete();

		log.info("Cluster {} for namespace {} undeployed.", cluster.getName(), cluster.getNamespace());
    }

	private Map<String, String> parameters(CockroachDBCluster cluster) {
		Map<String, String> parameters = new HashMap<>();
    	parameters.put(CLUSTER_NAME_PARAM_KEY, cluster.getName());
    	parameters.put(CLUSTER_UID_PARAM_KEY, cluster.getUid());
    	parameters.put(CLUSTER_STORAGE_PARAM_KEY, cluster.getStorage());

    	if(cluster.getBackup() !=null) {
        	parameters.put(BACKUP_DATABASE_PARAM_KEY, cluster.getBackup().getDatabases());
        	parameters.put(BACKUP_CRON_SCHEDULE_PARAM_KEY, cluster.getBackup().getCronSchedule());
        	parameters.put(BACKUP_MAX_KEPT_PARAM_KEY, cluster.getBackup().getMaxKeptBackups().toString());
        	parameters.put(BACKUP_S3_BUCKET_PARAM_KEY, cluster.getBackup().getStorage().getS3().getBucket());
        	parameters.put(BACKUP_S3_ROOT_PATH_PARAM_KEY, cluster.getBackup().getStorage().getS3().getRootPath());
        	parameters.put(BACKUP_S3_SETTINGS_SECRET_PARAM_KEY, cluster.getBackup().getStorage().getS3().getAwsSettingsSecret());    		
    	}

		return parameters;
	}
}