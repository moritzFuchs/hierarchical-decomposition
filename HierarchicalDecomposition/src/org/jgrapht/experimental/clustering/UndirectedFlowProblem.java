package org.jgrapht.experimental.clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import org.jgrapht.Graph;
import org.jgrapht.alg.EdmondsKarpMaximumFlow;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.experimental.util.LoggerFactory;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.google.common.collect.Lists;

/**
 * Solves a Max-Flow problem in a undirected Graph. Also includes tools for flow decompositions (min-cut , flow-paths)
 * 
 * @author moritzfuchs
 *
 * @param <V> : The type of vertices
 * @param <E> : The type of edges
 */
public class UndirectedFlowProblem<V extends Comparable<V> , E> implements FlowProblem<V,E>{

	private static final Logger LOGGER = LoggerFactory.getLogger(UndirectedFlowProblem.class.getName());

	/**
	 * The original (undirected) graph
	 */
	private Graph<V,E> g;
	
	/**
	 * Our directed copy of the undirected graph (each undirected edge in G gets 2 directed edges in this copy)
	 */
	private SimpleDirectedWeightedGraph<V,DefaultWeightedEdge> directedG;
	
	/**
	 * Source vertex of the single commodity problem
	 */
	private V source;
	
	/**
	 * Target vertex of the single commodity problem
	 */
	private V target;
	
	/**
	 * Map from edges in the directed graph to edges in the undirected graph
	 */
	private Map<DefaultWeightedEdge , E> directed_edges_map;

	/**
	 * Computed flow in the directed graph
	 */
	private Map<DefaultWeightedEdge, Double> directed_flow;

	/**
	 * Computed flow in the undirected graph (derived from the {@link UndirectedFlowProblem.undirected_flow})
	 */
	private HashMap<E, Double> undirected_flow;
	
	/**
	 * Computed min-cut of the undirected graph
	 */
	private Set<E> cut;

	/**
	 * Map from flow paths to their most congested edge
	 */
	private Map<List<V>, E> flowPathWeight;
	
	/**
	 * Construct the flow problem. Takes an undirected graph G and constructs a directed graph G' s.t. a max-flow-algorithm can be run on G'.
	 * 
	 * @param g : The original undirected graph G
	 * @param s : The source of the max-flow-problem
	 * @param t : The target of the max-flow-problem
	 */
	public UndirectedFlowProblem(Graph<V,E> g , V s , V t) {
		
		this.g = g;
		
		this.directedG = new SimpleDirectedWeightedGraph<V, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		this.directed_edges_map = new HashMap<DefaultWeightedEdge , E>();
		
		this.flowPathWeight = new HashMap<List<V> , E>();
		
		this.source = s;
		this.target = t;
		
		for (V v : g.vertexSet()) {
			this.directedG.addVertex(v);
		}
		
		for (E e : g.edgeSet()) {
			DefaultWeightedEdge new_edge = this.directedG.addEdge(g.getEdgeSource(e), g.getEdgeTarget(e));
			this.directedG.setEdgeWeight(new_edge, g.getEdgeWeight(e));
			this.directed_edges_map.put(new_edge, e);
			
			new_edge = this.directedG.addEdge(g.getEdgeTarget(e), g.getEdgeSource(e));
			this.directedG.setEdgeWeight(new_edge, g.getEdgeWeight(e));
			this.directed_edges_map.put(new_edge, e);
		}
	}
	
	/**
	 * Computes the max flow of the graph G. Uses {@link EdmondsKarpMaximumFlow} to compute the max-flow on the directed copy of the graph.
	 * The computed flow on the directed copy is then used to compute the flow for G as follows:
	 * If (u,v) has a flow of x and (v,u) has a flow of y then the edge {u,v} has flow of x-y or y-x depending on which vertex is the 'source' vertex in G
	 * 
	 * @return : The Max-Flow for the given Graph G and its source and target. (all 3 are received in the constructor) 
	 */
	@Override
	public Map<E , Double> getMaxFlow() {
		
		LOGGER.fine("Computing max flow for G = " + g);
		
		if (undirected_flow == null) {
			EdmondsKarpMaximumFlow<V, DefaultWeightedEdge> flowComputation = new EdmondsKarpMaximumFlow<V, DefaultWeightedEdge>(directedG);
			flowComputation.calculateMaximumFlow(source, target);
			directed_flow = flowComputation.getMaximumFlow();
			undirected_flow = new HashMap<E , Double>();
			
			for (DefaultWeightedEdge e:directed_flow.keySet()) {
				V source = directedG.getEdgeSource(e);
				
				E original_edge = directed_edges_map.get(e);
				
				Double weight = undirected_flow.get(original_edge);
				if (weight == null) {
					weight = 0.0;
				}
				
				if (g.getEdgeSource(original_edge).compareTo(source) == 0) {	
					undirected_flow.put(original_edge, weight + directed_flow.get(e));
				} else {
					undirected_flow.put(original_edge, weight - directed_flow.get(e));
				}
				
			}
		}
		
		LOGGER.fine("Max flow computation finished: " + undirected_flow);
		
		return undirected_flow;
	}

	/**
	 * Get the cut induced by the max-flow.
	 * 
	 * @return Set<E> : MinCut of G.
	 */
	public Set<E> getMinCut() {
		
		LOGGER.fine("Computing the min-cut based on the max flow");
		
		if (this.cut != null) {
			return cut;
		}
		
		if (undirected_flow == null) {
			getMaxFlow();
		}
		
		Queue<V> q = new LinkedList<V>();
		Set<E> potentialCutEdges = new HashSet<E>();
		Set<V> seen = new HashSet<V>();
		
		q.add(source);
		seen.add(source);
		
		while(!q.isEmpty()) {
			V v = q.poll();
			for (E out_edge : g.edgesOf(v)) {
				
				Double sign = 1.0;
				if (g.getEdgeTarget(out_edge).equals(v)) {
					sign = -1.0;
				}
				
				if (sign * undirected_flow.get(out_edge) < g.getEdgeWeight(out_edge)) {
					V next;
					if (sign == -1.0) {
						next = g.getEdgeSource(out_edge);
					} else {
						next = g.getEdgeTarget(out_edge);
					}
					
					if (!seen.contains(next)) {
						q.add(next);
						seen.add(next);
					}
				} else {
					potentialCutEdges.add(out_edge);
				}
			} 
		}
		
		this.cut = new HashSet<E>();
		
		for (E e : potentialCutEdges) {
			if (seen.contains(g.getEdgeSource(e)) && !seen.contains(g.getEdgeTarget(e))) {
				this.cut.add(e);
			}
			if (!seen.contains(g.getEdgeSource(e)) && seen.contains(g.getEdgeTarget(e))) {
				this.cut.add(e);
			}
		}
		
		LOGGER.fine("Min-Cut done: " + cut);
		
		return this.cut;
	}
	
//	/**
//	 * Computes the Min-Cut between the source and the target of G through the Min-Cut-Max-Flow-Theorem (See e.g. https://en.wikipedia.org/wiki/Max-flow_min-cut_theorem)
//	 * 
//	 * @return: The Min-Cut between source and target of G. 
//	 */
//	public Set<E> getCutOld() {
//		
//		LOGGER.fine("Computing the min-cut based on the max flow");
//		
//		if (this.cut != null) {
//			return cut;
//		}
//		
//		if (directed_flow == null) {
//			getMaxFlow();
//		}
//		
//		Queue<V> q = new LinkedList<V>();
//		Set<DefaultWeightedEdge> potentialCutEdges = new HashSet<DefaultWeightedEdge>();
//		Set<V> seen = new HashSet<V>();
//		
//		q.add(source);
//		seen.add(source);
//		
//		while(!q.isEmpty()) {
//			V v = q.poll();
//			for (DefaultWeightedEdge out_edge : directedG.outgoingEdgesOf(v)) {
//				if (directed_flow.get(out_edge) + EdmondsKarpMaximumFlow.DEFAULT_EPSILON < directedG.getEdgeWeight(out_edge)) {
//					V source_vertex = directedG.getEdgeTarget(out_edge);
//					if (!seen.contains(source_vertex)) {
//						q.add(source_vertex);
//						seen.add(source_vertex);
//					}
//				} else {
//					potentialCutEdges.add(out_edge);
//				}
//			} 
//		}
//		
//		this.cut = new HashSet<E>();
//		
//		for (DefaultWeightedEdge e : potentialCutEdges) {
//			if (seen.contains(directedG.getEdgeSource(e)) && !seen.contains(directedG.getEdgeTarget(e))) {
//				this.cut.add(directed_edges_map.get(e));
//			}
//		}
//		
//		LOGGER.fine("Min-Cut done: " + cut);
//		
//		return this.cut;
//	}
	
	/**
	 * Extracts a s-t-path, adds it to the paths array and reduces the flow along the extracted path
	 * 
	 * @param g : The graph
	 * @param flow : The flow we decompose
	 * @param t : The target t
	 * @param min_flow : The map of minimal flow to any vertex
	 * @param parent : The backtracking information (BFS tree with root s)
	 */
	public Set<FlowPath<V,E>> getPaths() {
		
		if (undirected_flow == null) {
			getMaxFlow();
		}
		
		Set<FlowPath<V,E>> paths = new HashSet<FlowPath<V,E>>();
		
		Map<E , Double> myflow = new HashMap<E,Double>(undirected_flow);
		Queue<V> q = new LinkedList<V>();
		Map<V , E> min_previous_edge = new HashMap<V , E>();
		Map<V,V> parent = new HashMap<V,V>();

		while (true) {
			parent.clear();
			min_previous_edge.clear();
			q.clear();
			
			parent.put(source,source);
			q.add(source);
			while (!q.isEmpty()) {
				V current_vertex = q.poll();
				
				//If we reach t we have found a s-t-path. Extract it!
				if (current_vertex.compareTo(target) == 0) {
					extractPath(g, myflow, target, min_previous_edge, parent,paths);
					break;
				}
				
				for(E e:g.edgesOf(current_vertex)) {
					
					V target = g.getEdgeTarget(e);
					V source = g.getEdgeSource(e);
					
					Double sign = 1.0;
					if (current_vertex.equals(target)) {
						sign = -1.0;
					}

					if (sign == 1.0) {
						//The target must be 'new' and flow must still be available
						if (parent.get(target) == null && myflow.get(e) != null && sign * myflow.get(e) > 0.0) {
							
							//remember parent for backtracking
							parent.put(target, current_vertex);
							
							//add target to queue for further inspection (BFS step) 
							q.add(target);
							
							E current_min_edge = min_previous_edge.get(current_vertex);
							
							if (current_min_edge == null || Math.abs(myflow.get(current_min_edge)) > sign * myflow.get(e)) {
								//remember vertex with minimal flow for this path
								min_previous_edge.put(target, e);
							} else {
								min_previous_edge.put(target, current_min_edge);
							}
						}
					} else {
						//The target must be 'new' and flow must still be available
						if (parent.get(source) == null && myflow.get(e) != null && sign * myflow.get(e) > 0.0) {
							
							//remember parent for backtracking
							parent.put(source, current_vertex);
							
							//add target to queue for further inspection (BFS step) 
							q.add(source);
							
							E current_min_edge = min_previous_edge.get(current_vertex);
							
							if (current_min_edge == null || Math.abs(myflow.get(current_min_edge)) > sign * myflow.get(e)) {
								//remember vertex with minimal flow for this path
								min_previous_edge.put(source, e);
							} else {
								min_previous_edge.put(source, current_min_edge);
							}
						}
					}
				}
			}
			
			//There are no more s-t-paths
			if (parent.get(target) == null)
				break;
		}
		
		return paths;
	}
	
	
	
//	public Set<FlowPath<V,E>> getPaths() {
//		
//		LOGGER.fine("Computing flow paths.");
//		
//		if (directed_flow == null) {
//			getFlow();
//		}
//		
//		Set<FlowPath<V,E>> paths = new HashSet<FlowPath<V,E>>();
//		
//		LOGGER.info("Retrieving flow paths between " + source + " and " + target + " for flow " + undirected_flow);
//		
//		//Get shallow copy of flow
//		Map<DefaultWeightedEdge , Double> myflow = new HashMap<DefaultWeightedEdge,Double>(directed_flow);
//		Queue<V> q = new LinkedList<V>();
//		Map<V , DefaultWeightedEdge> min_previous_edge = new HashMap<V , DefaultWeightedEdge>();
//		
//		Map<V,V> parent = new HashMap<V,V>();
//		
//		while (true) {
//			
//			parent.clear();
//			min_previous_edge.clear();
//			q.clear();
//			
//			parent.put(source,source);
//			
//			q.add(source);
//			while (!q.isEmpty()) {
//				V current_vertex = q.poll();
//				
//				//If we reach t we have found a s-t-path. Extract it!
//				if (current_vertex.compareTo(target) == 0) {
//					extractPath(directedG, myflow, target, min_previous_edge, parent,paths);
//					break;
//				}
//				
//				for(DefaultWeightedEdge e:directedG.outgoingEdgesOf(current_vertex)) {
//					
//					V target = directedG.getEdgeTarget(e);
//
//					//The target must be 'new' and flow must still be available
//					if (parent.get(target) == null && myflow.get(e) != null && myflow.get(e) > 0.0) {
//						
//						//remember parent for backtracking
//						parent.put(target, current_vertex);
//						
//						//add target to queue for further inspection (BFS step) 
//						q.add(target);
//						
//						DefaultWeightedEdge current_min_edge = min_previous_edge.get(current_vertex);
//						
//						if (current_min_edge == null || myflow.get(current_min_edge) > myflow.get(e)) {
//							//remember vertex with minimal flow for this path
//							min_previous_edge.put(target, e);
//						} else {
//							min_previous_edge.put(target, current_min_edge);
//						}
//					}
//				}
//			}
//			
//			//There are no more s-t-paths
//			if (parent.get(target) == null)
//				break;
//		}
//		
//		LOGGER.fine("Flow paths computed: " + paths);
//		
//		return paths;
//	}
	
	/**
	 * Extracts a s-t-path, adds it to the paths array and reduces the flow along the extracted path
	 * 
	 * @param g : The graph
	 * @param myflow : The flow we decompose
	 * @param t : The target t
	 * @param min_previous_edge : The map of minimal flow to any vertex
	 * @param parent : The backtracking information (BFS tree with root s)
	 */
	private void extractPath(
			Graph<V, E> g, 
			Map<E, Double> myflow, 
			V t, 
			Map<V, E> min_previous_edge, 
			Map<V, V> parent, 
			Set<FlowPath<V,E>>paths) {
		V v = t;
		E min_edge_on_path = min_previous_edge.get(v);
		Double flow_on_path = Math.abs(myflow.get(min_edge_on_path));
		List<V> path = new LinkedList<V>();
		
		while (parent.get(v) != v) {
			E e = g.getEdge(parent.get(v), v);
			//Substract flow of new path
			Double new_flow = null;
			
			if (v.equals(g.getEdgeSource(e))) {
				new_flow = myflow.get(e) + flow_on_path;
			} else {
				new_flow = myflow.get(e) - flow_on_path;
			}
			
			myflow.put(e, new_flow);
			path.add(v);
			v = parent.get(v);
				
		}
		path.add(v);
		
		//path is currently reversed, so we reverse it back
		path = Lists.reverse(path);
		
		flowPathWeight.put(path , directed_edges_map.get(min_edge_on_path));
		
		LOGGER.finest("Found s-t-path:" + path);
		
		FlowPath<V,E> path_object = new FlowPath<V,E>(path,flow_on_path);
		
		paths.add(path_object);

	}
	
	/**
	 * Returns the weight of the given flow path or null if the given list of vertices is not a flow path.
	 * The weight is computed as follows: During {@link UndirectedFlowProblem.getPaths()} the most congested edge of the path is saved. (the first one if there are several of them)
	 * If this method is called, this information is accessed. Hence the flow of the most congested edge is returned.
	 * CAUTION: If the flow is changed by the user (that's you!), the changed flow will be accessed. If the flow is scaled along paths, that will not mess up the validity of the 
	 * returned value. If however the flow is changed in any other way, the validity cannot be guaranteed any longer.
	 * 
	 * @return Returns the weight of a flow path. 
	 */
	@Override
	public Double getFlowPathWeight(FlowPath<V,E> path) {
		return path.getFlowPathWeight();
	}
	
	/**
	 * Decomposes the flow into a fractional, partial matching between vertices in A and vertices in B.
	 * 
	 * @param paths : The (potentially modified) flow paths 
	 * @return : A partial fractional matching
	 */
	@Override
	public Set<MatchedPair<V>> getFractionalPartialMatching(
			Set<FlowPath<V, E>> paths) {
		Set<MatchedPair<V>> matching = new HashSet<MatchedPair<V>>();
		
		//Each flow path corresponds to one  Matching edge.
		//A flow path has to following form: s -- x_e --- *** --- x_h --- t
		//The matching will be between x_e and x_h with weight equal to the weight of the flow path.
		for (FlowPath<V,E> path : paths) {
			V x_e = path.getPath().get(1);
			V x_h = path.getPath().get(path.getPath().size()-2);

			//Add match between x_e and x_h with weight of flow path to matching
			MatchedPair<V> match = new MatchedPair<V>(x_e ,x_h , getFlowPathWeight(path));
			matching.add(match);
		} 
		
		return matching;
	}
}
