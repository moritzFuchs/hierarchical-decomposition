package org.jgrapht.experimental.clustering;

import java.util.Map;
import java.util.Set;

import org.jgrapht.experimental.clustering.util.MatchedPair;

/**
 * Interface for a flow problem. 
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public interface FlowProblem<V,E> {

	/**
	 * Get the max-flow for the given problem
	 * 
	 * @return : Map from edges to their amount of flow
	 */
	public Map<E , Double> getMaxFlow();
	
	/**
	 * Get the min-cut for the given problem
	 * 
	 * @return : Set of edges that divide s from t
	 */
	public Set<E> getMinCut();
	
	/**
	 * Get the flow paths of the graph
	 * 
	 * @return : The set of flow paths (which are represented as List<V>)
	 */
	public Set<FlowPath<V,E>> getPaths();
	
	/**
	 * Get the flow path weight for a given path.
	 * 
	 * @param path : A flow path as computed by {@link FlowProblem.getPath}.
	 * @return : The weight of the given path
	 */
	public Double getFlowPathWeight(FlowPath<V,E> path);
	
	public Set<MatchedPair<V>> getFractionalPartialMatching(Set<FlowPath<V,E>> paths);
}
