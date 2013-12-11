package org.jgrapht.experimental.clustering;

import java.util.Observable;
import java.util.Set;

/**
 * Interface for clustering algorithm Thread. 
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public abstract class Clustering<V,E> extends Observable implements Runnable{

	/**
	 * The clustering task to be performed
	 */
	protected ClusteringTask<V,E> task;
	
	public Clustering(ClusteringTask<V,E> task) {
		this.task = task;
	}
		
	/**
	 * Starts the thread (calls the clustering algorithm and once this is done, notifies the listeners
	 */
	@Override
	public abstract void run();
	
	/**
	 * Get function for clustering
	 * 
	 * @return : The computed clustering
	 */
	public abstract Set<E> getClustering();

}
