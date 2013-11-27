package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.Set;

import org.jgrapht.experimental.clustering.BruteForceBisection;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

/**
 * Tests basic functionality of {@link BruteForceBisection}
 * 
 * @author moritzfuchs
 * @date 27.11.2013
 *
 */
public class BruteForceBisectionTest {

	@Test
	public void testEasyBisection() {
		
		SimpleGraph<Integer , DefaultWeightedEdge> g = new SimpleGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class); 
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		g.addVertex(7);
		g.addVertex(8);
		
		g.addEdge(1, 2);
		g.addEdge(1, 3);
		g.addEdge(2, 4);
		g.addEdge(3, 4);
		
		g.addEdge(4, 5);
		
		g.addEdge(5, 6);
		g.addEdge(5, 7);
		g.addEdge(6, 8);
		g.addEdge(7, 8);
		
		
		BruteForceBisection<Integer, DefaultWeightedEdge> bisec = new BruteForceBisection<Integer , DefaultWeightedEdge>(g);
		Set<Integer> cluster = bisec.computeBisection();
		
		System.out.println(cluster);
		System.out.println(cluster.size());
		
		assertTrue(cluster.size() == 4);
	}
	
	@Test
	public void testWeightedEasyBisection() {
		
		SimpleGraph<Integer , DefaultWeightedEdge> g = new SimpleGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class); 
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		g.addVertex(7);
		g.addVertex(8);
		
		DefaultWeightedEdge e;
		
		e = g.addEdge(1, 2);
		g.setEdgeWeight(e, 4.5);
		e = g.addEdge(1, 3);
		g.setEdgeWeight(e, 2.0);
		e = g.addEdge(2, 4);
		g.setEdgeWeight(e, 3.5);
		e = g.addEdge(3, 4);
		g.setEdgeWeight(e, 4.0);
		
		e =g.addEdge(4, 5);
		g.setEdgeWeight(e, 5.0);
		
		e = g.addEdge(5, 6);
		g.setEdgeWeight(e, 6.0);
		e = g.addEdge(5, 7);
		g.setEdgeWeight(e, 7.0);
		e = g.addEdge(6, 8);
		g.setEdgeWeight(e, 8.0);
		e = g.addEdge(7, 8);
		g.setEdgeWeight(e, 9.0);
		
		
		BruteForceBisection<Integer, DefaultWeightedEdge> bisec = new BruteForceBisection<Integer, DefaultWeightedEdge>(g);
		Set<Integer> cluster = bisec.computeBisection();
		
		System.out.println(cluster);
		System.out.println(cluster.size());
		
		assertTrue(cluster.size() == 4);
	}
}
