package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.old.DeletionStep;
import org.jgrapht.experimental.clustering.old.DummyFlow;
import org.jgrapht.experimental.clustering.old.FlowDecomposer;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class DeletionStepTest {

	@Test
	public void testFlowVectorMovement() {
		SimpleGraph<Integer , DefaultWeightedEdge> g = new SimpleGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		g.addVertex(7);
		g.addVertex(8);
		g.addVertex(9);
		
		DefaultWeightedEdge e12 = g.addEdge(1, 2);
		DefaultWeightedEdge e23 = g.addEdge(2, 3);
		
		DefaultWeightedEdge e14 = g.addEdge(1, 4);
		DefaultWeightedEdge e25 = g.addEdge(2, 5);
		DefaultWeightedEdge e36 = g.addEdge(3, 6);
		
		DefaultWeightedEdge e45 = g.addEdge(4, 5);
		DefaultWeightedEdge e56 = g.addEdge(5, 6);
		
		DefaultWeightedEdge e47 = g.addEdge(4, 7);
		DefaultWeightedEdge e58 = g.addEdge(5, 8);
		DefaultWeightedEdge e69 = g.addEdge(6, 9);
		
		DefaultWeightedEdge e78 = g.addEdge(7, 8);
		DefaultWeightedEdge e89 = g.addEdge(8, 9);
		
		SplitGraph<Integer, DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		SplitVertex<Integer , DefaultWeightedEdge> v;
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
		A.add(e23);
		
		A.add(e14);
		A.add(e36);
		
		A.add(e45);
		A.add(e56);
		
		A.add(e47);
		A.add(e58);
		
		A.add(e78);
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_t = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		
		A_t.add(gPrime.getSplitVertexFromEdge(e36));
				
		A_t.add(gPrime.getSplitVertexFromEdge(e47));
		A_t.add(gPrime.getSplitVertexFromEdge(e58));
		
		A_t.add(gPrime.getSplitVertexFromEdge(e78));
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_s = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		
		A_s.add(gPrime.getSplitVertexFromEdge(e14));
		A_s.add(gPrime.getSplitVertexFromEdge(e23));
		
		BiMap<DefaultWeightedEdge , Integer> edgeNum =  HashBiMap.create();
		Integer i = 0;
		for (DefaultWeightedEdge e : g.edgeSet()) {
			edgeNum.put(e, i++);
		}
		
		gPrime.addSourceAndTarget(A_s, A_t);
		
		DummyFlow<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> flow = 
			new DummyFlow<SplitVertex<Integer , DefaultWeightedEdge>, DefaultWeightedEdge>(gPrime, gPrime.getFlowSource(), gPrime.getFlowTarget());
		
		Integer m = g.edgeSet().size();
		
		Double[][] flowVectors = new Double[m][m];
		for (int k = 0;k<m;k++) {
			for (int l = 0;l<m;l++) {
				if (k == l)
					flowVectors[k][l] = 1.0;
				else
					flowVectors[k][l] = 0.0;
			}
		}

		FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> dec = new FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge>();		
		
		DeletionStep<Integer , DefaultWeightedEdge> del = new DeletionStep<Integer , DefaultWeightedEdge>(g, gPrime, edgeNum, flowVectors, m , A.size()); 
		
		Double [][] newFlowVectors = del.performDeletionStep(A, B, A_s, A_t, dec, flow.getFlow());
		
		for (int k=0;k<m;k++) {
			for (int l=0;l<m;l++) {
				if (k == l)
					assertTrue(newFlowVectors[k][l] == 1.0);
				else
					assertTrue(newFlowVectors[k][l] == 0.0);
			}
		}
		
		
		
		
		
	}
	
}
