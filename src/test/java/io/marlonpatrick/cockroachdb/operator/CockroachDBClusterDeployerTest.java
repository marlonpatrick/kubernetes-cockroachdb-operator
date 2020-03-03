package io.marlonpatrick.cockroachdb.operator;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;

public class CockroachDBClusterDeployerTest {

	CockroachDBCluster cluster;

	CockroachDBClusterDeployer deployer;

	@Rule
	public KubernetesServer server = new KubernetesServer(true, true);
	
	KubernetesClient client;

	@Before
	public void setUp() {
				
		this.cluster = new CockroachDBCluster();
		this.cluster.setName("cluster-test");
		this.cluster.setNamespace("namespace-test");
		this.cluster.setUid(UUID.randomUUID().toString());
		this.cluster.setStorage("1Gi");

		this.client = this.server.getClient();
		
		this.deployer = new CockroachDBClusterDeployer(this.client);
	}

	@Test
	public void testDeployJob() {
		this.deployer.deploy(this.cluster);
		
		StatefulSet statefulSet = this.client.apps().statefulSets().inNamespace(cluster.getNamespace())
				.withName(cluster.getName()).get();
		
		Assert.assertNotNull("Statefulset " + cluster.getName() + " not created.", statefulSet);

		boolean ownerReferenceMatch = statefulSet.getMetadata().getOwnerReferences().stream().allMatch(ownerReference -> ownerReference.getUid().equals(cluster.getUid()));
				
		Assert.assertTrue("Statefulset " + cluster.getName() + " exists, but with a different owner.", ownerReferenceMatch);		
	}
}
