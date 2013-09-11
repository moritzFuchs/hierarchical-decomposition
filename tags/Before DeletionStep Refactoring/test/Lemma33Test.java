package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.Connectivity;
import org.jgrapht.experimental.clustering.Lemma33;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

public class Lemma33Test {

	@Test
	public void simpleTest() {
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph(2);
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		
		A.add(g.getEdge(1, 3));
		A.add(g.getEdge(1, 4));
		A.add(g.getEdge(2, 4));
		
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
		
		B.add(g.getEdge(4, 6));
		B.add(g.getEdge(6, 7));
		
		Lemma33<Integer , DefaultWeightedEdge> lemma = new Lemma33<Integer , DefaultWeightedEdge>(g , A , B);
		lemma.performLemma33();
	
		assertTrue(Connectivity.isBalancedClustering(g, lemma.getF()));		
	}
	
}
