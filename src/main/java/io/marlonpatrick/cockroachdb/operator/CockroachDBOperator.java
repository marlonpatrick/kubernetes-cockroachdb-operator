package io.marlonpatrick.cockroachdb.operator;

import static io.radanalytics.operator.common.AnsiColors.re;
import static io.radanalytics.operator.common.AnsiColors.xx;
import static io.radanalytics.operator.common.AnsiColors.ye;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.radanalytics.operator.common.AbstractOperator;
import io.radanalytics.operator.common.Operator;

/**
 * <pre>
 * 	ABOUT SYNCHRONIZED METHODS:
 * 		
 * 		The default behavior of the abstract-operator library is that the onAdd, onModify and onDelete 
 * 		methods are synchronized (not through the word synchronized, but through the watcher 
 * 		implementation), that is, only 1 of these methods executes at a time. 
 * 		However, the fullReconciliation method is NOT synchronized. With that, the fullReconciliation 
 * 		method can execute while one of the other 3 methods is also executing. 
 * 		To avoid this situation, I made the 4 methods synchronized so that only 1 of these 4 methods 
 * 		can run at a time. In this way, situations are avoided where, for example, one method is adding 
 * 		a resource (onAdd) while another method (fullReconciliation) is at the same time trying to delete 
 * 		this same resource. 
 * 
 * </pre>
 * 
 * @author Marlon Patrick
 */
@Singleton
@Operator(forKind = CockroachDBCluster.class, prefix = "io.marlonpatrick")
//			additionalPrinterColumnNames = {"Workers", "Age"},
//			additionalPrinterColumnPaths = {".spec.worker.instances", ".metadata.creationTimestamp"},
//			additionalPrinterColumnTypes = {"string", "date"})
public class CockroachDBOperator extends AbstractOperator<CockroachDBCluster> {

	private static final Logger log = LoggerFactory.getLogger(CockroachDBOperator.class.getName());

	private RunningCockroachDBClusters runningClusters = new RunningCockroachDBClusters();

	private Map<String, CockroachDBClusterDeployer> deployers = new HashMap<>();

	// this.namespace
	// onInit()
	// fullReconciliation()

	protected synchronized void onAdd(CockroachDBCluster cluster) {
		log.info("onAdd: {}{} has been created in namespace {}: {}", prefix, entityName, namespace, cluster);
		log.info("onAdd: namespace: {}, entityName: {}, prefix: {}, cluster: {}", namespace, entityName,
				prefix, cluster);

		try {
			log.info("onAdd.SLEEP...: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}", namespace,
					entityName, prefix, this, cluster);
			Thread.sleep(35 * 1000);
			log.info("onAdd.WAKE_UP...: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}",
					namespace, entityName, prefix, this, cluster);
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.info("onAdd.SLEEP.ERROR: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}",
					namespace, entityName, prefix, this, cluster);
		}

//        KubernetesResourceList list = getDeployer().getResourceList(cluster);
//        client.resourceList(list).inNamespace(namespace).createOrReplace();
		runningClusters.put(namespace, cluster);
	}

	protected synchronized void onDelete(CockroachDBCluster cluster) {
		log.info("Existing CockroachDBCluster has been deleted: {}", cluster);
		log.info("onDelete: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}", namespace,
				entityName, prefix, this, cluster);

		try {
			log.info("onDelete.SLEEP...: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}",
					namespace, entityName, prefix, this, cluster);
			Thread.sleep(35 * 1000);
			log.info("onDelete.WAKE_UP...: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}",
					namespace, entityName, prefix, this, cluster);
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.info("onDelete.SLEEP.ERROR: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}",
					namespace, entityName, prefix, this, cluster);
		}

		runningClusters.remove(namespace, cluster.getName());
	}

	protected synchronized void onModify(CockroachDBCluster newCluster) {
		log.info("Existing CockroachDBCluster has been modified: {}", newCluster);
		log.info("onModify: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}", namespace,
				entityName, prefix, this, newCluster);

		try {
			log.info("onModify.SLEEP...: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}",
					namespace, entityName, prefix, this, newCluster);
			Thread.sleep(35 * 1000);
			log.info("onModify.WAKE_UP...: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}",
					namespace, entityName, prefix, this, newCluster);
		} catch (InterruptedException e) {
			e.printStackTrace();
			log.info("onModify.SLEEP.ERROR: namespace: {}, entityName: {}, prefix: {}, operator: {}, cluster: {}",
					namespace, entityName, prefix, this, newCluster);
		}

		CockroachDBCluster existingCluster = runningClusters.getCluster(namespace, newCluster.getName());

		if (existingCluster == null) {
			log.error("Something went wrong, unable to modify existing cluster. Perhaps it wasn't deployed properly.");
			return;
		}

		log.info("{}Recreating{} cluster  {}{}{}", re(), xx(), ye(), existingCluster.getName(), xx());

//        KubernetesResourceList list = getDeployer().getResourceList(newCluster);
//        
//        try {
//            client.resourceList(list).inNamespace(namespace).createOrReplace();
//        } catch (Exception e) {
//            log.warn("{}deleting and creating{} cluster  {}{}{}", re(), xx(), ye(), existingCluster.getName(), xx());
//            
//            client.resourceList(list).inNamespace(namespace).delete();
//            runningClusters.remove(namespace, existingCluster.getName());
//            
//            client.resourceList(list).inNamespace(namespace).createOrReplace();
//            runningClusters.put(namespace, newCluster);
//        }
	}

	@Override
	public synchronized void fullReconciliation() {
		// always namespace *

		log.info("Initiate fullReconciliation for CockroachDBOperator");
		log.info("fullReconciliation: namespace: {}, entityName: {}, prefix: {}, operator: {}", namespace, entityName,
				prefix, this);

//        if ("*".equals(namespace)) {
//            log.info("Skipping full reconciliation for namespace '*' (not supported)");
//            return;
//        }

		Set<CockroachDBCluster> desiredSet = super.getDesiredSet();

		log.info("CockroachDBOperator.fullReconciliation desiredSets in namespace {} {}", namespace, this);

		desiredSet.forEach(
				c -> log.info("CockroachDBOperator.fullReconciliation desiredSet: {} {}", namespace, c.getName()));			


//        log.info("Running full reconciliation for namespace {} and kind {}..", namespace, entityName);
//        
//        final AtomicBoolean change = new AtomicBoolean(false);
//        
//        Set<CockroachDBCluster> desiredSet = super.getDesiredSet();
//        
//        Map<String, CockroachDBCluster> desiredMap = desiredSet.stream().collect(Collectors.toMap(CockroachDBCluster::getName, Function.identity()));
//        
//        Map<String, Integer> actual = getActual();
//
//        log.debug("desired set: {}", desiredSet);
//        log.debug("actual: {}", actual);
//
//        Sets.SetView<String> toBeCreated = Sets.difference(desiredMap.keySet(), actual.keySet());
//        Sets.SetView<String> toBeDeleted = Sets.difference(actual.keySet(), desiredMap.keySet());
//
//        if (!toBeCreated.isEmpty()) {
//            log.info("toBeCreated: {}", toBeCreated);
//            change.set(true);
//        }
//        if (!toBeDeleted.isEmpty()) {
//            log.info("toBeDeleted: {}", toBeDeleted);
//            change.set(true);
//        }
//
//        // add new
//        toBeCreated.forEach(cluster -> {
//            log.info("creating cluster {}", cluster);
//            onAdd(desiredMap.get(cluster));
//        });
//
//        // delete old
//        toBeDeleted.forEach(cluster -> {
//            SparkCluster c = new SparkCluster();
//            c.setName(cluster);
//            log.info("deleting cluster {}", cluster);
//            onDelete(c);
//        });
//
//        // first reconciliation after (re)start -> update the clusters instance
//        if (!fullReconciliationRun) {
//            getClusters().resetMetrics();
//            desiredMap.entrySet().forEach(e -> getClusters().put(e.getValue()));
//        }
//
//        if (!change.get()) {
//            log.info("no change was detected during the reconciliation");
//        }
	}

	private CockroachDBClusterDeployer getDeployer() {

//    	CockroachDBClusterDeployer deployer = deployers.get(namespace);
//    	
//        if (deployer == null) {
//            deployer = new CockroachDBClusterDeployer(client, entityName, prefix, namespace);
//            deployers.put(namespace, deployer);
//        }
//        
//        return deployer;

		return deployers.get(namespace);
	}
}