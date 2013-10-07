package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.krv.MatchingMatrix;
import org.jgrapht.experimental.clustering.krv.MatchingStepNew;
import org.jgrapht.experimental.clustering.old.MatchingStep;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Test;

import com.google.common.collect.BiMap;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class MatchingStepNewTest {

	
	@Test
	public void testMatchingStep() {
		
		SimpleWeightedGraph<Integer , DefaultWeightedEdge> g = new SimpleWeightedGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		
		DefaultWeightedEdge e12 = g.addEdge(1, 2);
		DefaultWeightedEdge e23 = g.addEdge(2, 3);
		DefaultWeightedEdge e13 = g.addEdge(1, 3);
		
		g.setEdgeWeight(e12, 2.0);
		g.setEdgeWeight(e23, 100.0);
		
		SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>> p = 
				new MatchedPair<SplitVertex<Integer, DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e12), gPrime.getSplitVertexFromEdge(e23), 2.0);
		
		
		
		Map<DefaultWeightedEdge , Integer> edgeNum = new HashMap<DefaultWeightedEdge , Integer>();
		edgeNum.put(e12, 0);
		edgeNum.put(e23, 1);
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		A.add(e12);
		A.add(e23);
		A.add(e13);
		
		Set<MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>> S = 
				new HashSet<MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>>();
		S.add(p);
		
		MatchingStepNew<Integer , DefaultWeightedEdge> m = new MatchingStepNew<Integer , DefaultWeightedEdge>(edgeNum, g.edgeSet().size());
		m.computeMatchingMatrix(gPrime, A, S);
		
		SparseDoubleMatrix2D matrix = m.getMatrix();
		
		for (int row=0;row<matrix.rows();row++) {
			assertTrue(matrix.viewRow(row).zSum() == 1.0 || matrix.viewRow(row).zSum() == 0.0);
		}
		
	}
}
