package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.old.VerticeDivider;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;


public class VerticeDividerTest {

	@Test
	public void simpleTest() {
		
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph(10);
		
		SplitGraph<Integer,DefaultWeightedEdge> gPrime = new SplitGraph<Integer,DefaultWeightedEdge>(g);
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>(g.edgeSet());
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
		
		Integer m = g.edgeSet().size();

		Map<DefaultWeightedEdge , Integer> edgeNum = generator.generateEdgeNum();
		Double[][] flowVectors = generator.generateTrivialFlowVectors();
		
		int i = 0;
		while (i < 10000) {
			VerticeDivider<Integer , DefaultWeightedEdge> divider = new VerticeDivider<Integer, DefaultWeightedEdge>(flowVectors , m , edgeNum);
			divider.divideActiveVertices(gPrime, A);
	
//			System.out.println(division.getFirst().size() + " : " + division.getFirst());
//			System.out.println(division.getSecond().size() + " : " + division.getSecond());
			
			assertTrue(divider.getAs().size() > 0);
			assertTrue(divider.getAt().size() > 0);
			
			assertTrue(divider.getAs().size() <= Math.ceil ( (double)A.size() / 8));
			assertTrue(divider.getAt().size() >= A.size() / 2);
			
			i++;
		}
		
	}
	
}
