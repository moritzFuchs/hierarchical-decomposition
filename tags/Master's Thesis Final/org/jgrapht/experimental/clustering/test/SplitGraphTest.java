package org.jgrapht.experimental.clustering.test;


import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.Util;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;


public class SplitGraphTest {

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
		
		SplitGraph<Integer,DefaultWeightedEdge> gPrime = new SplitGraph<Integer,DefaultWeightedEdge>(g);
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
		
		A.add(e1);
		A.add(e2);
		A.add(e3);
		
		B.add(e5);
		B.add(e6);
		
		gPrime.addSourceAndTarget(gPrime.getSplitVertices(A), gPrime.getSplitVertices(B));
		
		assertTrue(Util.connected(gPrime, gPrime.getFlowSource() , gPrime.getFlowTarget()));
		
	}
	
}
