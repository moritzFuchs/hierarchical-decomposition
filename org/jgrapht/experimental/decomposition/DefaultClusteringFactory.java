package org.jgrapht.experimental.decomposition;

import org.jgrapht.experimental.clustering.Clustering;
import org.jgrapht.experimental.clustering.ClusteringTask;

/**
 * Factory class for a {@link Clustering}. Creates {@link org.jgrapht.experimental.clustering.partitionA} clustering classes. 
 * 
 * @author moritzfuchs
 *
 * @param <V>
 * @param <E>
 */
public class DefaultClusteringFactory<V,E> implements ClusteringFactory<V, E> {

	
	//TODO : Create and return clustering
	/**
	 * Creates a {@link Clustering} for the given graph G. 
	 * 
	 * @param g : The graph we want to decompose
	 */
	@Override
	public Clustering<V, E> getClustering(ClusteringTask<V,E> task) {
		
		return null;
	}

}
