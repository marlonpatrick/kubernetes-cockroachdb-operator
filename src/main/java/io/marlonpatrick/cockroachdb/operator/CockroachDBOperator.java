package io.marlonpatrick.cockroachdb.operator;

import static io.radanalytics.operator.common.AnsiColors.re;
import static io.radanalytics.operator.common.AnsiColors.xx;
import static io.radanalytics.operator.common.AnsiColors.ye;

import java.util.Set;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.radanalytics.operator.common.AbstractOperator;
import io.radanalytics.operator.common.CustomResourceWatcher;
import io.radanalytics.operator.common.Operator;
import io.radanalytics.operator.common.crd.InfoClass;

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
@Operator(forKind = CockroachDBCluster.class, prefix = "io.marlonpatrick", crd = true)
//			additionalPrinterColumnNames = {"Workers", "Age"},
//			additionalPrinterColumnPaths = {".spec.worker.instances", ".metadata.creationTimestamp"},
//			additionalPrinterColumnTypes = {"string", "date"})
public class CockroachDBOperator extends AbstractOperator<CockroachDBCluster> {

	private static final Logger log = LoggerFactory.getLogger(CockroachDBOperator.class.getName());

	private final RunningCockroachDBClusters runningClusters = new RunningCockroachDBClusters();

	private CockroachDBClusterDeployer deployer;
	
	@Override
	protected void onInit() {
		this.deployer = new CockroachDBClusterDeployer(this.client);
	}

    protected CockroachDBCluster convertCr(InfoClass info) {
    	CockroachDBCluster cluster = CustomResourceWatcher.defaultConvert(CockroachDBCluster.class, info);
    	cluster.setUid(info.getMetadata().getUid());
    	return cluster;
    }

	protected synchronized void onAdd(CockroachDBCluster cluster) {
		log.info("onAdd: {}{} has been created in namespace {}: {}", prefix, entityName, cluster.getNamespace(), cluster);
        
        deployer.deploy(cluster);
        
		runningClusters.put(cluster);
	}

	protected synchronized void onDelete(CockroachDBCluster cluster) {
		log.info("onDelete: Existing {}{} has been deleted in namespace {}: {}", prefix, entityName, cluster.getNamespace(), cluster);
		runningClusters.remove(cluster.getNamespace(), cluster.getName());
	}

	protected synchronized void onModify(CockroachDBCluster newCluster) {
		log.info("onModify: Existing {}{} has been modified in namespace {}: {}", prefix, entityName, newCluster.getNamespace(), newCluster);

		CockroachDBCluster existingCluster = runningClusters.getCluster(newCluster.getNamespace(), newCluster.getName());

		if (existingCluster == null) {
			log.error("onModify: Unable to modify existing {}{} in namespace {}. Perhaps it wasn't deployed properly.", prefix, entityName, newCluster.getNamespace());
			return;
		}

		log.info("{}Recreating{} cluster  {}{}{}", re(), xx(), ye(), existingCluster.getName(), xx());
//
//        KubernetesResourceList list = getDeployer().getResourceList(newCluster);
//        
//        try {
//            client.resourceList(list).inNamespace(newCluster.getNamespace()).createOrReplace();
//        } catch (Exception e) {
//            log.warn("{}deleting and creating{} cluster  {}{}{}", re(), xx(), ye(), existingCluster.getName(), xx());
//            
//            client.resourceList(list).inNamespace(newCluster.getNamespace()).delete();
//            runningClusters.remove(newCluster.getNamespace(), existingCluster.getName());
//            
//            client.resourceList(list).inNamespace(newCluster.getNamespace()).createOrReplace();
//            runningClusters.put(newCluster);
//        }
	}

	@Override
	public synchronized void fullReconciliation() {
		log.info("Starting full reconciliation for {} in namespace {}", prefix, entityName, namespace);

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
}