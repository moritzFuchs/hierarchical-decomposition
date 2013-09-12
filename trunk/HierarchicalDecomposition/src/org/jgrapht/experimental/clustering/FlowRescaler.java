package org.jgrapht.experimental.clustering;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Bundles algorithms to rescale a given flow in G'_st as follows: For all edges s -- x_e with flow >= 0.5 rescale the corresponding flow paths such that s -- x_e holds 1 unit of flow 
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class FlowRescaler<V extends Comparable<V>,E> {

	/**
	 * Rescales a given flow in G'_st as follows: For all edges s -- x_e with flow >= RESCALE_BOUND rescale the corresponding flow paths such that s -- x_e holds 1 unit of flow
	 * 
	 * @param gPrime
	 * @param flow
	 * @return 
	 */
	public Set<FlowPath<SplitVertex<V,E>,DefaultWeightedEdge>> rescaleFlow(SplitGraph<V,E> gPrime , Map<DefaultWeightedEdge , Double> flow , FlowProblem<SplitVertex<V,E>, DefaultWeightedEdge> flow_problem) {
		
		//Decompose flow paths
		Set<FlowPath<SplitVertex<V,E>, DefaultWeightedEdge>> paths = flow_problem.getPaths();
		
		//Rescale the resulting flow: All flow paths with flow > 1/2 from s to x_e \in A_s get scaled up to have flow 1		
		Map<DefaultWeightedEdge , Double> pathFactors = new HashMap<DefaultWeightedEdge , Double>();
		
		//Find out which edges have to be scaled
		for (DefaultWeightedEdge e : gPrime.edgesOf(gPrime.getFlowSource())) {
			if (flow.get(e) >= DecompositionConstants.RESCALE_BOUND)
				pathFactors.put(e, 1 / flow.get(e));
		}
		
		//Next we need to scale each path accordingly 
		Map<DefaultWeightedEdge , Double> newFlow = new HashMap<DefaultWeightedEdge , Double>(); 
		
		//Initiate new flow for all edges
		for(DefaultWeightedEdge e : gPrime.edgeSet()) {
			newFlow.put(e, 0.0);
		}
		
		//scale paths according to gathered information
		for (FlowPath<SplitVertex<V,E>,DefaultWeightedEdge> path : paths) {
			SplitVertex<V,E> s = path.getPath().get(0);
			SplitVertex<V,E> x_e = path.getPath().get(1);
			if (pathFactors.get(gPrime.getEdge(s, x_e)) != null) {
				scalePath(gPrime , path , flow , newFlow , pathFactors.get(gPrime.getEdge(s, x_e)), flow_problem);
			} else {
				scalePath(gPrime , path , flow , newFlow , 1.0,flow_problem);
			}
		}
		
		//Fix capacities of gPrime to make flow feasible again
		fixCapacities(gPrime,newFlow);
		
		return paths;
	}

	/**
	 * Raises the edge capacity for each edge that carries more flow than it has capacities
	 * 
	 * @param gPrime : The subdivision graph G' of G
	 * @param newFlow : The scaled flow between s and t
	 */
	private void fixCapacities(SplitGraph<V, E> gPrime, Map<DefaultWeightedEdge, Double> newFlow) {
		
		//Raise edge capacities in G' to get feasible flow
		for (DefaultWeightedEdge e : newFlow.keySet()) {
			if (newFlow.get(e) > gPrime.getEdgeWeight(e)) {
				gPrime.setEdgeWeight(e, newFlow.get(e));
			}
		}
	}

	/**
	 * Scale a flow path by a given factor
	 * 
	 * @param gPrime : The subdivision flow-graph G'_st of G
	 * @param path : An s-t-flow-path in G'_st
	 * @param flow : A feasible s-t-flow in G'_st
	 * @param newFlow : The new rescaled flow which will be returned in the end
	 * @param factor : The factor by which the s-t-flow-path will be scaled up
	 * @param dec : The flow decomposer which caches flow path weights
	 */
	private void scalePath(SplitGraph<V,E> gPrime , FlowPath<SplitVertex<V, E> , DefaultWeightedEdge> path,
			Map<DefaultWeightedEdge, Double> flow,
			Map<DefaultWeightedEdge, Double> newFlow, Double factor, FlowProblem<SplitVertex<V, E>, DefaultWeightedEdge> flow_problem) {
		
		path.rescalePath(factor);
		/*
		SplitVertex<V,E> v = path.getPath().get(0);
		for (int i=1;i<path.size();i++) {
			SplitVertex<V,E> w = path.get(i);
			DefaultWeightedEdge e = gPrime.getEdge(v,w);
			
			
			
			newFlow.put(e, newFlow.get(e) + flow_problem.getFlowPathWeight(path) * factor);
			
			v = w;
		}
		*/
	}
}
