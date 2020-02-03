package io.marlonpatrick.cockroachdb.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.batch.Job;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.utils.Serialization;

class CockroachDBClusterDeployer {

	private static final Logger log = LoggerFactory.getLogger(CockroachDBClusterDeployer.class.getName());

	private static final String CLUSTER_NAME_PARAM_KEY = "cockroachdbcluster.name";
	private static final String CLUSTER_UID_PARAM_KEY = "cockroachdbcluster.uid";
	private static final String CLUSTER_STORAGE_PARAM_KEY = "cockroachdbcluster.storage";
	private static final String CLUSTER_STATEFULSET_YAML_FILE_PATH = "/cockroachdb-deploy/cockroachdb-statefulset.yaml";
	private static final String CLUSTER_JOB_INIT_YAML_FILE_PATH = "/cockroachdb-deploy/cockroachdb-cluster-init.yaml";

	private KubernetesClient client;

    CockroachDBClusterDeployer(KubernetesClient client) {
        this.client = client;
    }

    void deploy(CockroachDBCluster cluster) {

		log.info("Deploying cluster {} in namespace {}.", cluster.getName(), cluster.getNamespace());

    	Map<String, String> parameters = parameters(cluster);

		List<HasMetadata> resourceList = new ArrayList<HasMetadata>();
		resourceList.addAll(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_STATEFULSET_YAML_FILE_PATH), parameters));
		resourceList.add(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_JOB_INIT_YAML_FILE_PATH), Job.class, parameters));
		
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
		resourceList.addAll(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_STATEFULSET_YAML_FILE_PATH), parameters));
		resourceList.addAll(Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream(CLUSTER_JOB_INIT_YAML_FILE_PATH), parameters));

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
		return parameters;
	}
}