package org.jgrapht.experimental.clustering;

import org.jgrapht.Graph;

/**
 * Container for a decomposition taks. Contains the graph to be decomposed as well as its {@link TreeVertex} in the decomposition tree. 
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class ClusteringTask<V,E> {

	protected Graph<V,E> graph;
	
	/**
	 * Create new DecompositionTask containing a graph and its corresponding {@link TreeVertex}
	 * 
	 * @param g : A graph
	 * @param parent : The corresponding {@link TreeVertex} of the given graph
	 */
	public ClusteringTask(Graph<V,E> g) {
		this.graph = g;
	}
	
	/**
	 * Returns the graph for this task
	 * @return : The graph for this task
	 */
	public Graph<V,E> getGraph() {
		return graph;
	}

}
