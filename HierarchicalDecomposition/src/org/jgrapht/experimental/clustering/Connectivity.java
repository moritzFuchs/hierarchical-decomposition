package org.jgrapht.experimental.clustering;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;

/**
 * Bundles all connectivity methodes:
 *  * Check for balanced clusterings
 *  * Check if there is a s-t-flow-path  
 * 
 * @author moritzfuchs
 *
 */
public class Connectivity {
	
	/**
	 * Each cluster must be smaller than  BALANCED_CLUSTERING_UPPER_BOUND * |V| for the clustering to be 'balanced'
	 */
	private static final double BALANCED_CLUSTERING_UPPER_BOUND = 3.0/4.0;
	
	
	/**
	 * Checks if there is a s-t-flow > 0 in g 
	 * 
	 * @param g : Graph G
	 * @param s : Starting vertex s
	 * @param t : target vertex t
	 * @param edges : forbidden edges
	 * @return : true is t is reachable from s without using forbidden edges, false otherwise
	 */
	public static <V,E> Boolean hasFlowPath(Graph<V,E> g , V s , V t) {
				
			Queue<V> q = new LinkedList<V>();
			q.add(s);
			
			Set<V> seen = new HashSet<V>();
			seen.add(s);
			
			while(!q.isEmpty()) {
				V current_vertex = q.poll();
				//We have found t => s and t must be connected
				if (current_vertex == t)
					return true;
				
				for (E e : g.edgesOf(current_vertex) ) {
					
					if (g.getEdgeWeight(e) > 0.0) { 
						
						V target;
						if (g.getEdgeTarget(e) != current_vertex) {
							target = g.getEdgeTarget(e);
						} else {
							target = g.getEdgeSource(e);
						}
						if (!seen.contains(target)) {
							seen.add(target);
							q.add(target);
						}
						
					}
				}
				
			}
			
			return false;
		}

	
	/**
	 * Checks whether a given clustering is balanced or not. A clustering is balanced if every induced cluster has size at most 3/4  * V[G].
	 * 
	 * @param g
	 * @param edges
	 * @return
	 */
	public static <V,E> Boolean isBalancedClustering(Graph<V,E> g , Set<E> edges) {
		
		Set<V> visited = new HashSet<V>();
		
		for (V v: g.vertexSet()) {
			if (!visited.contains(v)) {
				Set<V> reachable_nodes = Connectivity.bfs(g , v , visited , edges);

				
				if (reachable_nodes.size() > g.vertexSet().size() * BALANCED_CLUSTERING_UPPER_BOUND) {
					return false;
				}
			}
		}
		
		//If we did not return false after seeing all vertices the clustering has to be balanced
		return true;
	}
	
	/**
	 * Gets a graph G and a set of edges \subset E[G] and returns the induced clusters of all given edges are cut
	 * 
	 * @param <V> : The type of vertices
	 * @param <E> : The type of edges
	 * @param g : The graph G
	 * @param cut_edges : The set of cut edges that induce the clustering
	 * @return : The set of clusters induced by the set of cut edges
	 */
	public static <V extends Comparable<V>,E> Set<Graph<V,E>> getClusters(Graph<V,E> g , Set<E> cut_edges) {
		
		Set<Graph<V , E>> clusters = new HashSet<Graph<V , E>>();
		Set<V> visited = new HashSet<V>();
		
		for (V v : g.vertexSet()) {
			if (!visited.contains(v)) {
				//FIXME: Vertices in reachable_nodes might be copies of vertices in G. Make sure they are the same Object!
				Set<V> reachable_nodes = Connectivity.bfs(g, v, visited, cut_edges);
				clusters.add(SubGraphGenerator.generateSubGraph(g, reachable_nodes));
			}
		}
		
		return clusters;
	}
	
	/**
	 * Perform BFS, ignore edges that induce clustering (given by 'edges') and count reachable vertices that have not been seen before. 
	 * 
	 * @param g : graph to perform BFS on
	 * @param v : starting vertex from G
	 * @param visited : markers for vertices to indicate whether or not a given vertex has been visited before
	 * @param edges : edges that induce clustering
	 * @return All reachable nodes
	 */
	public static <V,E> Set<V> bfs(Graph <V,E> g , V v , Set<V> visited , Set<E> edges) {
		//Initially we have found ourself
		Set<V> nodesFound = new HashSet<V>();
		
		Queue<V> q = new LinkedList<V>();
		q.add(v);
		
		while (!q.isEmpty()) {
			V current_vertex = q.poll();
			if (!visited.contains(current_vertex)) {
				
				nodesFound.add(current_vertex);
				for (E e : g.edgesOf(current_vertex)) {
					
					//Skip e \in edges (= edges that are deleted to induce clustering)
					if (edges.contains(e))
						continue;
					
					V target;
					if (g.getEdgeTarget(e) != current_vertex){
						target = g.getEdgeTarget(e);
					} else {
						target = g.getEdgeSource(e);
					}
					q.add(target);
				}
			}
			
			//Mark current vertex as visited
			visited.add(current_vertex);
		}
		
		return nodesFound;
	}
	
	/**
	 * Computes the weight of outgoing edges 
	 * 
	 * @param <V> : The type of vertices
	 * @param <E> : The type of edges
	 * @param g : The graph G
	 * @param vertices : The vertex set S
	 * @return : The weight of edges between S and V[G]\S
	 */
	public static <V extends Comparable<V>,E> Double getOutDegree(Graph<V,E> g , Set<V> vertices) {
		
		Double weight = 0.0;
		
		for (V source : vertices) {
			for (E e : g.edgesOf(source)) {
				V target = Util.getEdgeTarget(g, source, e);
				if (!vertices.contains(target)) {
					weight += g.getEdgeWeight(e);
				}
			}
		}
		
		return weight;
	}
	
	/**
	 * Computes the weight of outgoing edges 
	 * 
	 * @param <V> : The type of vertices
	 * @param <E> : The type of edges
	 * @param g : The graph G
	 * @param vertices : The vertex set S
	 * @return : The weight of edges between S and V[G]\S
	 */
	public static <V,E> Double getOutDegree(Graph<V,E> g , V vertex) {
		
		Double weight = 0.0;

		for (E e : g.edgesOf(vertex)) {
			weight += g.getEdgeWeight(e);
		}		
		return weight;
	}
}
