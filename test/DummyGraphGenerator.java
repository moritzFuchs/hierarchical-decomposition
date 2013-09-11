package org.jgrapht.experimental.clustering.test;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;

/**
 * Generates a Graph for testing purposes
 * 
 * @author moritzfuchs
 *
 */
public class DummyGraphGenerator extends AbstractGraphGenerator {


	public DummyGraphGenerator() {
		
	}
	
	/**
	 * Generate a Graph with 1 + 3 * level vertices and 5 * level vertices 
	 * 
	 * @param level
	 * @return
	 */
	public Graph<Integer, DefaultWeightedEdge> generateGraph() {
		
		g = new SimpleGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		
		DefaultWeightedEdge e1,e2,e3,e4,e5,e6,e7;
		
		e1 = g.addEdge(1, 2);
		g.setEdgeWeight(e1, 1);
		
		e2 = g.addEdge(1, 3);
		g.setEdgeWeight(e2, 2);
		
		e3 = g.addEdge(2, 4);
		g.setEdgeWeight(e3, 1);
		
		e4 = g.addEdge(3, 4);
		g.setEdgeWeight(e4, 1);
		
		e5 = g.addEdge(3, 5);
		g.setEdgeWeight(e5, 1);
		
		e6 = g.addEdge(4, 6);
		g.setEdgeWeight(e6, 2);
		
		e7 = g.addEdge(5, 6);
		g.setEdgeWeight(e7, 2);
		
		return g;
	}
	
	
	/**
	 * Generate a Graph with 1 + 3 * level vertices and 5 * level vertices 
	 * 
	 * @param level
	 * @return
	 */
	public DirectedGraph<Integer, DefaultWeightedEdge> generateDirectedGraph() {
		
		SimpleDirectedGraph<Integer, DefaultWeightedEdge> g = new SimpleDirectedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		
		DefaultWeightedEdge e1,e2,e3,e4,e5,e6,e7;
		
		e1 = g.addEdge(1, 2);
		g.setEdgeWeight(e1, 1);
		
		e2 = g.addEdge(1, 3);
		g.setEdgeWeight(e2, 2);
		
		e3 = g.addEdge(2, 4);
		g.setEdgeWeight(e3, 1);
		
		e4 = g.addEdge(3, 4);
		g.setEdgeWeight(e4, 1);
		
		e5 = g.addEdge(3, 5);
		g.setEdgeWeight(e5, 1);
		
		e6 = g.addEdge(4, 6);
		g.setEdgeWeight(e6, 2);
		
		e7 = g.addEdge(5, 6);
		g.setEdgeWeight(e7, 2);
		
		return g;
	}
}

