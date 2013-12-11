package org.jgrapht.experimental.decomposition;

import org.jgrapht.experimental.clustering.Clustering;
import org.jgrapht.experimental.clustering.ClusteringTask;

/**
 * Interface for a Clustering factory. 
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public interface ClusteringFactory<V,E> {
	public Clustering<V,E> getClustering(ClusteringTask<V,E> task);
}
