package org.jgrapht.experimental.decomposition;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.ClusteringTask;
import org.jgrapht.experimental.clustering.TreeVertex;

/**
 * 
 * Extension of the usual {@link ClusteringTask} which adds a {@link TreeVertex} to the container in order to remember the 
 * parent in the {@link DecompositionTree}. 
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class DecompositionTask<V,E> extends ClusteringTask<V,E>{

	/**
	 * The corresponding {@link TreeVertex} to the graph defined in {@link ClusteringTask}
	 */
	private TreeVertex<V> parent;
	
	private Graph<V,E> parentGraph;
	
	public DecompositionTask(Graph<V,E> g , Graph<V, E> subG , TreeVertex<V> parent) {
		super(subG);
		this.parentGraph = g;
		this.parent = parent;
	}

	/**
	 * Returns the corresponding {@link TreeVertex} to the graph
	 * @return : The corresponong {@link TreeVertex} to the graph
	 */
	public TreeVertex<V> getParent() {
		return parent;
	}
	
	@Override
	public Graph<V,E> getGraph() {
		return parentGraph;
	}
	
	public Graph<V,E> getSubGraph() {
		return this.graph;
	}
}
