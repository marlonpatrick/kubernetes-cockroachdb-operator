package io.marlonpatrick.cockroachdb.operator;

import java.util.HashMap;
import java.util.Map;

class RunningCockroachDBClusters {

    private final Map<String, Map<String, CockroachDBCluster>> clusters = new HashMap<>();
    
    void put(CockroachDBCluster cluster) {
    	Map<String, CockroachDBCluster> clustersInNamespace = clusters.get(cluster.getNamespace());
    	
    	if(clustersInNamespace == null) {
    		clustersInNamespace = new HashMap<>();
    		clusters.put(cluster.getNamespace(), clustersInNamespace);
    	}
    	
    	clustersInNamespace.put(cluster.getName(), cluster);
    }

    void remove(String namespace, String clusterName) {
    	Map<String, CockroachDBCluster> clustersInNamespace = clusters.get(namespace);
    	
    	if(clustersInNamespace == null) {
    		return;
    	}
    	
    	clustersInNamespace.remove(clusterName);
    }

    CockroachDBCluster getCluster(String namespace, String clusterName) {
    	Map<String, CockroachDBCluster> clustersInNamespace = clusters.get(namespace);
    	
    	if(clustersInNamespace == null) {
    		return null;
    	}
    	
    	return clustersInNamespace.get(clusterName);
    }
}
