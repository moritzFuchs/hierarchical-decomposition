package org.jgrapht.experimental.clustering.test;


import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.Separator;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

import org.junit.Test;

import com.google.common.collect.BiMap;

public class SeparatorTest {

	@Test
	public void testSimpleWeightedGraph() {
		SimpleGraph<Integer , DefaultWeightedEdge> g = new SimpleGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		
		DefaultWeightedEdge e1 = g.addEdge(1, 2);
		g.setEdgeWeight(e1, 10);
		DefaultWeightedEdge e2 = g.addEdge(2, 3);
		g.setEdgeWeight(e2, 8);
		DefaultWeightedEdge e3 = g.addEdge(3, 4);
		g.setEdgeWeight(e3, 7);
		
		SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		A.add(e1);
		
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
		B.add(e3);
		
		Separator<Integer , DefaultWeightedEdge> s = new Separator<Integer , DefaultWeightedEdge>();
		Set<DefaultWeightedEdge> separator = s.computeSeparator(g, gPrime, A, B);
		
		
		assertFalse(separator.isEmpty());
		assertTrue(separator.contains(e3));
		assertFalse(separator.contains(e2));
		assertFalse(separator.contains(e1));
	}
	
	@Test
	public void testDummyGraph() {
		DummyGraphGenerator generator = new DummyGraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph();
		
		SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		
		DefaultWeightedEdge e1 = g.getEdge(1 , 2);
		DefaultWeightedEdge e2 = g.getEdge(3 , 5);
		A.add(e1);
		A.add(e2);
		
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
		
		DefaultWeightedEdge e3 = g.getEdge(4 , 6);
		B.add(e3);
		
		Separator<Integer , DefaultWeightedEdge> s = new Separator<Integer , DefaultWeightedEdge>();
		Set<DefaultWeightedEdge> separator = s.computeSeparator(g, gPrime, A, B);
		
		assertFalse(separator.isEmpty());
		
		A.add(e1);
		A.add(e2);

		B.add(e3);
		
		gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		gPrime.addSourceAndTargetForLemma33(gPrime.getSplitVertices(A), gPrime.getSplitVertices(B));

		assertFalse(connected(gPrime, gPrime.getFlowSource(), gPrime.getFlowTarget(), separator));
	}
	
	
	/**
	 * Checks if the given set of edges of separates s and t.
	 * 
	 * @param gPrime : A {@link SplitGraph} G' of the original Graph G
	 * @param s : The starting vertex in G'
	 * @param t : The target vertex in G'
	 * @param forbidden : The set of forbidden edges in the original G (= forbidden SplitGraphVertices in G')
	 * @return true if t is reachable without using forbidden edges , false otherwise
	 */
	private Boolean connected(SplitGraph<Integer , DefaultWeightedEdge> gPrime, SplitVertex<Integer , DefaultWeightedEdge> s , SplitVertex<Integer , DefaultWeightedEdge> t , Set<DefaultWeightedEdge> forbidden) {
		
		Queue<SplitVertex<Integer , DefaultWeightedEdge>> q = new LinkedList<SplitVertex<Integer , DefaultWeightedEdge>>();
		q.add(s);
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> seen = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		seen.add(s);
		
		while(!q.isEmpty()) {
			SplitVertex<Integer , DefaultWeightedEdge> current_vertex = q.poll();
			//We have found t => s and t must be connected
			if (current_vertex == t)
				return true;
			
			for (DefaultWeightedEdge e : gPrime.edgesOf(current_vertex) ) {
				
				SplitVertex<Integer , DefaultWeightedEdge> target;
				if (gPrime.getEdgeTarget(e) != current_vertex) {
					target = gPrime.getEdgeTarget(e);
				} else {
					target = gPrime.getEdgeSource(e);
				}
				
				BiMap<DefaultWeightedEdge, SplitVertex<Integer, DefaultWeightedEdge>> edgeMap = gPrime.getEdgeBiMap();
				
				DefaultWeightedEdge edge = edgeMap.inverse().get(target);
				
				if (forbidden.contains(edge))
					continue;

				if (!seen.contains(target)) {
					seen.add(target);
					q.add(target);
				}
			}
		}
		return false;
	}

}
