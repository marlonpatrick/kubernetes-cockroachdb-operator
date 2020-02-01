package io.marlonpatrick.cockroachdb.operator;

import java.util.HashMap;
import java.util.Map;

public class RunningCockroachDBClusters {

    private final Map<String, Map<String, CockroachDBCluster>> clusters = new HashMap<>();
    
    public void put(String namespace, CockroachDBCluster cluster) {
    	Map<String, CockroachDBCluster> clustersInNamespace = clusters.get(namespace);
    	
    	if(clustersInNamespace == null) {
    		clustersInNamespace = new HashMap<>();
    		clusters.put(namespace, clustersInNamespace);
    	}
    	
    	clustersInNamespace.put(cluster.getName(), cluster);
    }

    public void remove(String namespace, String clusterName) {
    	Map<String, CockroachDBCluster> clustersInNamespace = clusters.get(namespace);
    	
    	if(clustersInNamespace == null) {
    		return;
    	}
    	
    	clustersInNamespace.remove(clusterName);
    }

    public CockroachDBCluster getCluster(String namespace, String clusterName) {
    	Map<String, CockroachDBCluster> clustersInNamespace = clusters.get(namespace);
    	
    	if(clustersInNamespace == null) {
    		return null;
    	}
    	
    	return clustersInNamespace.get(clusterName);
    }
}
