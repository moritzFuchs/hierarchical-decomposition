package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.Util;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

public class UtilTest {

	@Test
	public void simpleTest() {
		DummyGraphGenerator generator = new DummyGraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph();
		
		System.out.println(Util.getEdgeTarget(g, 1, g.getEdge(1, 2)));
		
		assertTrue(2 == Util.getEdgeTarget(g, 1, g.getEdge(1, 2)));
		
		
	}
	
}
