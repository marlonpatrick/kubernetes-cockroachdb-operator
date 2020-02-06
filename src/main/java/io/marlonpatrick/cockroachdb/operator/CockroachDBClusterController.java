package io.marlonpatrick.cockroachdb.operator;

import static io.radanalytics.operator.common.AnsiColors.re;
import static io.radanalytics.operator.common.AnsiColors.xx;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.radanalytics.operator.common.AbstractOperator;
import io.radanalytics.operator.common.CustomResourceWatcher;
import io.radanalytics.operator.common.Operator;
import io.radanalytics.operator.common.crd.InfoClass;

/**
 * <pre>
 * 	Marlon Patrick 01/Feb/2020: ABOUT SYNCHRONIZED METHODS:
 * 		
 * 		The default behavior of the abstract-operator library is that the onAdd, onModify and onDelete 
 * 		methods are synchronized (not through the word synchronized), that is, only 1 of these methods 
 * 		executes at a time. 
 * 		However, the fullReconciliation method is NOT synchronized by default. With that, the 
 * 		fullReconciliation method can execute while one of the other 3 methods is also executing. 
 * 		To avoid this situation, I made the 4 methods synchronized so that only 1 of these 4 methods 
 * 		can run at a time. In this way, situations are avoided where, for example, one method is adding 
 * 		a resource (onAdd) while another method (fullReconciliation) is at the same time trying to delete 
 * 		this same resource.
 * </pre>
 */
@Singleton
@Operator(forKind = CockroachDBCluster.class, prefix = "io.marlonpatrick", crd = true)
public class CockroachDBClusterController extends AbstractOperator<CockroachDBCluster> {

	private static final Logger log = LoggerFactory.getLogger(CockroachDBClusterController.class.getName());

	private final Map<String, CockroachDBCluster> runningClusters = new HashMap<>();

	private CockroachDBClusterDeployer deployer;

	@Override
	protected void onInit() {
		this.deployer = new CockroachDBClusterDeployer(this.client);
	}

	protected CockroachDBCluster convertCr(@SuppressWarnings("rawtypes") InfoClass info) {
		CockroachDBCluster cluster = CustomResourceWatcher.defaultConvert(CockroachDBCluster.class, info);
		cluster.setUid(info.getMetadata().getUid());
		return cluster;
	}

	protected synchronized void onAdd(CockroachDBCluster cluster) {
		log.info("onAdd: {}{} named {} has been created in namespace {}.", prefix, entityName, cluster.getName(),
				cluster.getNamespace());
		log.debug("onAdd: {}", cluster);
		
		StatefulSet statefulSet = this.client.apps().statefulSets().inNamespace(cluster.getNamespace())
				.withName(cluster.getName()).get();

		if (statefulSet == null) {

			deployer.deploy(cluster);

			runningClusters.put(cluster.getUid(), cluster);

			log.info("onAdd: cluster {} for namespace {} registered.", cluster.getName(), cluster.getNamespace());

		} else {

			boolean ownerReferenceMatch = statefulSet.getMetadata().getOwnerReferences().stream().allMatch(ownerReference -> ownerReference.getUid().equals(cluster.getUid()));
			
			if (ownerReferenceMatch) {

				runningClusters.put(cluster.getUid(), cluster);

				log.info(
						"onAdd: cluster {} already deployed in namespace {} before operator restart.",
						cluster.getName(), cluster.getNamespace());
				
				log.info("onAdd: cluster {} for namespace {} registered.", cluster.getName(), cluster.getNamespace());

			} else {
				log.error(
						"{}onAdd: {}{} named {} in namespace {} it will not be deployed because there are already objects like StatefulSet with the same name but a different UID. Please adjust your cluster settings.{}",
						re(), prefix, entityName, cluster.getName(), cluster.getNamespace(), xx());
			}
		}
	}

	protected synchronized void onDelete(CockroachDBCluster cluster) {
		log.info("onDelete: Existing {}{} named {} has been deleted in namespace {}.", prefix, entityName,
				cluster.getName(), cluster.getNamespace());
		log.debug("onDelete: {}", cluster);

		runningClusters.remove(cluster.getUid());

		log.info("onDelete: cluster {} for namespace {} unregisted.", cluster.getName(), cluster.getNamespace());
	}

	/**
	 * Dummy onModify: always undeploy and redeploy
	 */
	protected synchronized void onModify(CockroachDBCluster newCluster) {
		CockroachDBCluster existingCluster = runningClusters.get(newCluster.getUid());

		log.info("onModify: Existing {}{} named {} has been modified in namespace {}.", prefix, entityName,
				newCluster.getName(), newCluster.getNamespace());
		log.debug("onModify: New Cluster: {}", newCluster);
		log.debug("onModify: Existing cluster: {}", existingCluster);

		if (existingCluster == null) {
			log.error(
					"onModify: Unable to modify cluster {} in namespace {} with uid {}. Perhaps it wasn't deployed properly.",
					newCluster.getName(), newCluster.getNamespace(), newCluster.getUid());
			return;
		}


		log.info("onModify: {}Recreating{} cluster {} in namespace {}.", re(), xx(), newCluster.getName(),
				newCluster.getNamespace());

		deployer.undeploy(existingCluster);
		runningClusters.remove(existingCluster.getUid());

		onAdd(newCluster);
	}

	@Override
	public synchronized void fullReconciliation() {
		log.info("Running full reconciliation for {}{} in namespace {}.", prefix, entityName, namespace);

		Map<String, CockroachDBCluster> desiredMap = super.getDesiredSet().stream()
				.collect(Collectors.toMap(CockroachDBCluster::getUid, Function.identity()));

		log.debug("fullReconciliation: Current operator state for namespace {}: {}", namespace,
				runningClusters.values());
		log.debug("fullReconciliation: Desired operator state for namespace {}: {}", namespace, desiredMap.values());

		boolean hasDeletes = deleteOnFullReconciliation(desiredMap);

		boolean hasAdds = addOnFullReconciliation(desiredMap);

		if (!hasDeletes && !hasAdds) {
			log.info(
					"fullReconciliation: No change was detected during the reconciliation of CRD {}{} for namespace {}.",
					prefix, entityName, namespace);
		}

		log.info("Finisinhg full reconciliation for {}{} in namespace {}.", prefix, entityName, namespace);
	}

	private boolean addOnFullReconciliation(Map<String, CockroachDBCluster> desiredMap) {

		final AtomicBoolean change = new AtomicBoolean(false);

		desiredMap.entrySet().forEach(e -> {
			if (!runningClusters.containsKey(e.getKey())) {
				log.info("fullReconciliation: Deploying cluster {} in namespace {}.", e.getValue().getName(),
						e.getValue().getNamespace());
				onAdd(e.getValue());
				change.set(true);
			}
		});

		return change.get();
	}

	private boolean deleteOnFullReconciliation(Map<String, CockroachDBCluster> desiredMap) {
		final AtomicBoolean change = new AtomicBoolean(false);

		runningClusters.entrySet().forEach(e -> {
			if (!desiredMap.containsKey(e.getKey())) {
				log.info("fullReconciliation: Unregistering cluster {} in namespace {}.", e.getValue().getName(),
						e.getValue().getNamespace());
				onDelete(e.getValue());
				change.set(true);
			}
		});

		return change.get();
	}
}