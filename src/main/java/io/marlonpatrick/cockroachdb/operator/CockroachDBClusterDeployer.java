package io.marlonpatrick.cockroachdb.operator;

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

	private KubernetesClient client;

    CockroachDBClusterDeployer(KubernetesClient client) {
        this.client = client;
    }

    void deploy(CockroachDBCluster cluster) {
//        synchronized (this.client) {
//        	
//        }
    	
    	Map<String, String> parameters = new HashMap<>();
    	parameters.put("cockroachdbcluster.name", cluster.getName());
    	parameters.put("cockroachdbcluster.uid", cluster.getUid());
    	parameters.put("cockroachdbcluster.storage", cluster.getStorage());

		List<HasMetadata> resourceList = Serialization.unmarshal(CockroachDBClusterDeployer.class.getResourceAsStream("/cockroachdb-deploy/cockroachdb-statefulset.yaml"), parameters);

		log.info("deploy: cluster: {}, parameters: {}, resourceList: {}", cluster, parameters, resourceList);

        client.resourceList(resourceList).inNamespace(cluster.getNamespace()).createOrReplace();
    }
}
