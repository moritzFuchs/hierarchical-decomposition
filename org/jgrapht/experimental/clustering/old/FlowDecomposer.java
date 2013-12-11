package org.jgrapht.experimental.clustering.old;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.Util;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.experimental.util.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * Given a graph G and a feasible flow f on G computes:
 * 	* A flow path decomposition
 * 	* The induces cut C  
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class FlowDecomposer<V extends Comparable<V>,E> {

	/**
	 * Local logger
	 */
	private final static Logger LOGGER = LoggerFactory.getLogger(FlowDecomposer.class
		      .getName());
	
	/**
	 * The cut computed through flow decomposition
	 */
	private Set<E> cut = new HashSet<E>();
	
	/**
	 * The flow paths computed through flow decomposition (max. m of them)
	 */
	private Set<List<V>> paths = new HashSet<List<V>>();
	
	/**
	 * The flow path weights
	 */
	private Map<List<V> , Double> flowPathWeight = new HashMap<List<V> , Double>();
	
	/**
	 * Decomposes the flow into a fractional, partial matching between vertices in A and vertices in B.
	 * 
	 * @return
	 */
	public Set<MatchedPair<V>> getFractionalPartialMatching(Graph<V,E> g ,  Set<V> A , Set<V> B , Map<E,Double> flow , V s, V t) {
		Set<MatchedPair<V>> matching = new HashSet<MatchedPair<V>>();
		
		//Each flow path corresponds to one  Matching edge.
		//A flow path has to following form: s -- x_e --- *** --- x_h --- t
		//The matching will be between x_e and x_h with weight equal to the weight of the flow path.
		for (List<V> path : getFlowPaths(g,flow,s,t)) {
			V x_e = path.get(1);
			V x_h = path.get(path.size()-2);

			//Add match between x_e and x_h with weight of flow path to matching
			MatchedPair<V> match = new MatchedPair<V>(x_e ,x_h , getFlowPathWeight(path));
			matching.add(match);
		} 
		
		return matching;
	}
	
	/**
	 * Computes a flow path decomposition of the given flow
	 * 
	 * @param g : A graph
	 * @param myflow : A s-t-flow on the given graph G
	 * @param s : Source s of the s-t-flow
	 * @param t : Target t of the the s-t-flow
	 * @return : flow paths of the given flow
	 */
	public Set<List<V>> getFlowPaths(Graph<V,E> g , Map<E , Double> flow, V s, V t) {
	
		LOGGER.info("Retrieving flow paths between " + s + " and " + t + " for flow " + flow);
		
		//Get shallow copy of flow
		Map<E , Double> myflow = new HashMap<E,Double>(flow);
		Queue<V> q = new LinkedList<V>();
		Map<V , Double> min_flow = new HashMap<V , Double>();
		
		while (true) {
			
			Map<V,V> parent = new HashMap<V,V>();
			parent.put(s,s);
			
			q.add(s);
			while (!q.isEmpty()) {
				V current_vertex = q.poll();
				
				//If we reach t we have found a s-t-path. Extract it!
				if (current_vertex == t) {
					extractPath(g, myflow, t, min_flow, parent);
					break;
				}
				
				for(E e:g.edgesOf(current_vertex)) {
					
					V target = Util.getEdgeTarget(g, current_vertex, e);

					//The target must be 'new' and flow must still be available
					if (parent.get(target) == null && myflow.get(e) != null && myflow.get(e) > 0.0) {
						
						//remember parent for backtracking
						parent.put(target, current_vertex);
						//add target to queue for further inspection (BFS step) 
						q.add(target);
						
						
						Double current_min = min_flow.get(current_vertex);
						if (current_min == null)
							current_min = Double.POSITIVE_INFINITY;
						
						//remember minimal flow to this node
						min_flow.put(target, Math.min(current_min, myflow.get(e)));
					}
				}
			}
			
			//There are no more s-t-paths
			if (parent.get(t) == null)
				break;
		}
		
		return paths;
	}

	/**
	 * Extracts a s-t-path, adds it to the paths array and reduces the flow along the extracted path
	 * 
	 * @param g : The graph
	 * @param flow : The flow we decompose
	 * @param t : The target t
	 * @param min_flow : The map of minimal flow to any vertex
	 * @param parent : The backtracking information (BFS tree with root s)
	 */
	private void extractPath(Graph<V, E> g, Map<E, Double> flow, V t, Map<V, Double> min_flow, Map<V, V> parent) {
		V v = t;
		Double flow_on_path = min_flow.get(v);
		List<V> path = new LinkedList<V>();
		
		while (parent.get(v) != v) {
			E e = g.getEdge(parent.get(v), v);
			//Substract flow of new path
			Double new_flow = flow.get(e) - flow_on_path;
			
			flow.put(e, new_flow);
			path.add(v);
			v = parent.get(v);
				
		}
		path.add(v);
		
		//path is currently reversed, so we reverse it back
		path = Lists.reverse(path);
		
		flowPathWeight.put(path , flow_on_path);
		
		LOGGER.fine("Found s-t-path:" + path);
		
		paths.add(path);
	}
	
	/**
	 * Computes the cut induced by the max-flow
	 * WARNING: WRONG!
	 * 
	 * @param g : The graph
	 * @param flow : The flow along each edge
	 * @param s : The source of the flow in G 
	 * @return : The induced cut
	 */
	@Deprecated
	public Set<E> getCutOld(Graph<V,E> g , Map<E , Double> flow, V s) {
		LOGGER.info("Retrieving cut from flow.");
		
		//Get shallow copy of flow
		flow = new HashMap<E,Double>(flow);
		
		//start dfs from s, look for edges with no capacity left
		dfs(g,flow,s,new HashMap<V,Boolean>());
		
		LOGGER.fine("cut = " + cut);
		
		return cut;
	}
	
	/**
	 * Computes the cut induced by the max-flow
	 * WARNING: WRONG
	 * 
	 * @param g : The graph G
	 * @param flow : The max flow on G
	 * @param s : Flow source on G
	 * @param t : Flow target of G
	 * @return : The induced min-cut
	 */
	@Deprecated
	public Set<E> getCutV2(Graph<V,E> g , Map<E , Double> flow, V s , V t) {
		
		Set<E> edges = new HashSet<E>();
		
		Set<List<V>> p = this.getFlowPaths(g, flow, s, t);
		
		for (List<V> path : p) {
			Double weight = getFlowPathWeight(path);
			Integer current = 0;
			while (current+1 < path.size()-1 && weight != flow.get(g.getEdge(path.get(current), path.get(current+1)))) {
				current++;
			}
			
			System.out.println(weight + " " + flow.get(g.getEdge(path.get(current), path.get(current+1))));
			
			if (current <= path.size()-2)
				edges.add(g.getEdge(path.get(current), path.get(current+1)));
		}
		
		return edges;
	}
	
	public Set<E> getCut(Graph<V,E> g , Map<E , Double> flow, V s , V t) {
		
		Set<V> seen = new HashSet<V>();
		
		Queue<V> q = new LinkedList<V>();
		q.add(s);
		
		seen.add(s);
		//BFS on edges with capacity remaining
		while (!q.isEmpty()) {
			V current_vertex = q.poll();
			for (E e : g.edgesOf(current_vertex)) {
				V target = Util.getEdgeTarget(g, current_vertex, e);
				
				if (!seen.contains(target) && flow.get(e) < g.getEdgeWeight(e)) {
					seen.add(target);
					q.add(target);
				}
			}
		}
		
		Set<E> cut = new HashSet<E>();
		
		for (V v : seen) {
			for (E e: g.edgesOf(v)) {
				V target = Util.getEdgeTarget(g, v, e);
				if (!seen.contains(target)) {
					cut.add(e);
				}
			}
		}

		return cut;
	}
	
	
	/**
	 * Performs a DFS Step for the Cut decomposition algorithm
	 * 
	 * @param g
	 * @param flow
	 * @param v
	 * @param seen
	 */
	private void dfs(Graph<V,E> g , Map<E , Double> flow, V v ,Map<V, Boolean> seen) {
		seen.put(v, true);
		for (E e:g.edgesOf(v)) {
			
			//If the edge has no capacity left it is part of the cut
			if (g.getEdgeWeight(e) == flow.get(e)) {
				System.out.println(e + " - " + flow.get(e)+ "/" + g.getEdgeWeight(e) );
				cut.add(e);
			} else {
				//Else recurse
				V target = Util.getEdgeTarget(g, v, e);
				
				if (seen.get(target) == null && flow.get(e) > 0.0) {
					dfs(g , flow , target , seen);
					seen.put(target , true);
				}
			}
		}
	}
	
	/**
	 * Returns the weight of a given flow path as computed by getFlowPaths
	 * 
	 * @param path
	 * @return
	 */
	public Double getFlowPathWeight(List<V> path) {
		return flowPathWeight.get(path); 
	}
}
