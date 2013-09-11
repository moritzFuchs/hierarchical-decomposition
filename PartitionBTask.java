package org.jgrapht.experimental.clustering;

import java.util.Set;

import org.jgrapht.Graph;

public class PartitionBTask<V, E> extends ClusteringTask<V,E>{

	/**
	 * The observed subgraph
	 */
	private Graph<V,E> subG;
	
	/**
	 * The procomputed clustering of {@link PartitionA}
	 */
	private Set<E> clustering;
	
	public PartitionBTask(Graph<V,E> g , Graph<V,E> subG , Set<E> clustering) {
		super(g);
		
		this.subG = subG;
		this.clustering = clustering;
	}
	
	/**
	 * Returns the precomputed clustering for the PartitionB Taks
	 * 
	 * @return : The precomputed clustering of {@link PartitionA}
	 */
	public Set<E> getClustering() {
		return clustering;
	}
	
	/**
	 * Returns the subgraph of G that is currently observed
	 * 
	 * @return : The observed subgraph
	 */
	public Graph<V,E> getSubGraph() {
		return subG;
	}
	
}
