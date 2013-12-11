package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.experimental.clustering.BalancedClustering;
import org.jgrapht.experimental.clustering.Connectivity;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;


/**
 * Tests the BalancedClustering Class = tests if the method isBalancedClustering is correct
 * 
 * @author moritzfuchs
 *
 */
public class BalancedClusteringTest {

	@Test
	public void simpleTest() {
	
		SimpleGraph<Integer , DefaultWeightedEdge> g = new SimpleGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
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
	
		Set<DefaultWeightedEdge> edges = new HashSet<DefaultWeightedEdge>();
		edges.add(e1);
		edges.add(e2);
		
		BalancedClustering<Integer , DefaultWeightedEdge> balancedClustering = new BalancedClustering<Integer, DefaultWeightedEdge>();
		
		assertFalse(balancedClustering.isBalancedClustering(g, edges));
		assertFalse(Connectivity.isBalancedClustering(g, edges));
		
		edges.clear();
		
		edges.add(e3);
		edges.add(e4);
		edges.add(e5);
		
		assertTrue(balancedClustering.isBalancedClustering(g, edges));
		assertTrue(Connectivity.isBalancedClustering(g, edges));

	}
	
}
