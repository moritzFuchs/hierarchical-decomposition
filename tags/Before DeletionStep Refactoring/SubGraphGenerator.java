package org.jgrapht.experimental.clustering;

import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleGraph;

/**
 * Generator for Subgraphs. Gets a graph G and a set of vertices V' of G and returns a copy of the induced subgraph G[V']. 
 * Vertices of the generated subgraph are also vertices on the original Graph G. However, edges of G[V'] are copies of 
 * edges of the original graph G 
 * 
 * @author moritzfuchs
 *
 * @param <V> The vertex type
 * @param <E> The edges type
 */
public class SubGraphGenerator<V,E> {

	/**
	 * Takes a graph G and a set of vertices V' and returns the induced subgraph G[V']
	 * 
	 * @param g : A graph G
	 * @param vertices : The set of vertices V' the subgraph is based on
	 * @return : The induced subgraph G[V']
	 * @throws Exception 
	 */
	public static <V extends Comparable<V>,E> Graph<V,E> generateSubGraph(Graph<V,E> g , Set<V> vertices){
		
		SimpleGraph<V,E> subgraph = new SimpleGraph<V,E>(g.getEdgeFactory());
		
		for (V current_vertex: vertices) {
			subgraph.addVertex(current_vertex);
			for (E e : g.edgesOf(current_vertex)) {
				V target = Util.getEdgeTarget(g, current_vertex, e);
				if (subgraph.containsVertex(target)) {
					E new_edge = subgraph.addEdge(current_vertex, target);
					subgraph.setEdgeWeight(new_edge, g.getEdgeWeight(e));
				}
			}
		}
		return subgraph;
	}
}
