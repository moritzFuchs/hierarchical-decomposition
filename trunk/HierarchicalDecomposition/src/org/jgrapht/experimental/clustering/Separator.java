package org.jgrapht.experimental.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Given a graph and two sets of edges A and B, this class computes an edge separator with the following properties:
 *  * It separates A from B (every path between A and B contains at least 1 edge of the resulting set)
 *  * x_e \in X_Y sends one unit of flow; x_a \in X_A receives at most 6 units of flow. This can be routed with congestion O(log n)
 *  * x_e \in X_Y sends one unit of flow; x_b \in X_B receives at most 6 units of flow. This can be routed with congestion O(log n)
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class Separator<V extends Comparable<V>,E> {

	/**
	 * The edge separator
	 */
	private Set<SplitVertex<V,E>> X_Y;

	/**
	 * Computes an edge separator X_Y according to Lemma A2. For the computed set of edges holds:
	 *  * It separates A from B (every path between A and B contains at least 1 edge of the resulting set)
	 *  * x_e \in X_Y sends one unit of flow; x_a \in X_A receives at most 6 units of flow. This can be routed with congestion O(log n)
	 *  * x_e \in X_Y sends one unit of flow; x_b \in X_B receives at most 6 units of flow. This can be routed with congestion O(log n)
	 *  
	 * @param g : The original Graph G
	 * @param gPrime : The subdivision Graph G' of G
	 * @param A : The edge set A \subset V[G]
	 * @param B : The edge set B \subset V[G]
	 */
	public Set<E> computeSeparator(Graph<V,E> g , SplitGraph<V,E> gPrime, Set<E> A , Set<E> B) {
		
		X_Y = new HashSet<SplitVertex<V,E>>();
		
		gPrime.addSourceAndTargetForLemma33(gPrime.getSplitVertices(A),gPrime.getSplitVertices(B));

		while (Util.connected(gPrime , gPrime.getFlowSource(), gPrime.getFlowTarget())) {
			iterate(g , gPrime , A , B);
		}
		
		Set<E> C = gPrime.getOriginalEdges(X_Y);
		
		//Cleanup time! remove source and target again
		gPrime.removeSourceAndTarget();
		
		return C;

	}
	
	/**
	 * Computes flow from s to t and removes cut vertices x_e that handle more than cap(e) / 2 of flow
	 * 
	 * @param g : The graph G
	 * @param gPrime : The SplitGraph G' of G
	 * @param A : A set of edges of G
	 * @param B : A set of edges of G
	 */
	private void iterate(Graph<V,E> g, SplitGraph<V,E> gPrime , Set<E> A , Set<E> B) {
		//Compute the flow
		FlowProblem<SplitVertex<V,E> , DefaultWeightedEdge> flow_problem = new UndirectedFlowProblem<SplitVertex<V,E> , DefaultWeightedEdge>(gPrime,gPrime.getFlowSource() , gPrime.getFlowTarget());
		Map<DefaultWeightedEdge , Double> maxFlow = flow_problem.getMaxFlow();
		
		//Decompose the flow into flowPaths and a cut
		
		Set<FlowPath<SplitVertex<V, E>>> pathSet = flow_problem.getPaths();
		Set<DefaultWeightedEdge> cut = flow_problem.getMinCut();
		
		//Get SplitGraphVertices incident to cut that represent edges in G
		Set<SplitVertex<V,E>> cutVertices = new HashSet<SplitVertex<V,E>>();

		for (DefaultWeightedEdge e : cut) {
			cutVertices.add(gPrime.getIncidentSplitVertex(e));
		}
		
		
		Map<SplitVertex<V,E> , Double> flowAssignment = new HashMap<SplitVertex<V,E> , Double>();

		//Assign each flow path (its weight) to the first cut vertex representing an edge in G on the path
		for (FlowPath<SplitVertex<V,E>> path : pathSet) {
			
			SplitVertex<V,E> first = getFirstCutOnPath(path.getPath() , cutVertices);
			if (flowAssignment.get(first) == null) {
				flowAssignment.put(first, 0.0);
			}
			
			//Assign flow path weight to first cut vertex on path as computed by getFirstSplitVertexOnPath
			flowAssignment.put(first, flowAssignment.get(first) + flow_problem.getFlowPathWeight(path));
		}
		
		//remove vertices with more than cap(e) / 2 of flow path weights assigned to it
		for(SplitVertex<V,E> v : flowAssignment.keySet()) {
			if (flowAssignment.get(v) >= g.getEdgeWeight(gPrime.getOriginalEdge(v)) / 2) {
				X_Y.add(v);
				gPrime.removeVertex(v);
			}
		} 

		//finally reduce the capacity of edges in A and B by the amount of flow routed through them
		reduceCapacity(gPrime , A , maxFlow);
		reduceCapacity(gPrime , B , maxFlow);
	}
	
	/**
	 * Takes a splitGraph G' and a set of edges of the original graph G and reduces the capacity of all edges in G' that are incident to some SplitGraphVertex that represents an edge from the given set.
	 * 
	 * @param gPrime : The subdivision graph  G' of G
	 * @param A : A set of edges of G
	 * @param maxFlow : a flow on G'
	 */
	private void reduceCapacity(SplitGraph<V,E> gPrime , Set<E> A , Map<DefaultWeightedEdge , Double> maxFlow ) {
		for(SplitVertex<V, E> v : gPrime.getSplitVertices(A)) {
			DefaultWeightedEdge e = gPrime.getEdge(v , gPrime.getFlowTarget());

			//v might have been removed, in this case e will be null
			if (e != null) {
			
				Double localFlow = maxFlow.get(e);
				//reduce capacity of vertices incident to t by the flow between t and v
				for (DefaultWeightedEdge incident : gPrime.edgesOf(v)) {
					gPrime.setEdgeWeight(incident, gPrime.getEdgeWeight(incident) - localFlow);
				}
			}
		}
	}
	
	/**
	 * Returns the first cut on a path p (or null is p is not cut)  
	 * 
	 * @param path : A path in G'
	 * @param cut : A set of vertices V of G'
	 * @return : First occurence of any vertex in V on p (or null in there is no vertex of V on p)
	 */
	private SplitVertex<V,E> getFirstCutOnPath(List<SplitVertex<V, E>> path , Set<SplitVertex<V, E>> cut) {
		for (int i=1; i<path.size();i++) {
			if (cut.contains(path.get(i)))
				return path.get(i);
		}
		
		return null;
	}
	
}
