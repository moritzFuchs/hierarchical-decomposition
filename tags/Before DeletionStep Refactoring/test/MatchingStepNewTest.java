package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.MatchingMatrix;
import org.jgrapht.experimental.clustering.MatchingStepNew;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.old.MatchingStep;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import com.google.common.collect.BiMap;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class MatchingStepNewTest {

	@Test
	public void testMatchingStep() {
		DummyGraphGenerator generator = new DummyGraphGenerator();
		Graph<Integer, DefaultWeightedEdge> g = generator.generateGraph();
		
		SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		BiMap<DefaultWeightedEdge, Integer> edgeNum = generator.generateEdgeNum();
		
		MatchingStepNew<Integer, DefaultWeightedEdge> matchingStep = new MatchingStepNew<Integer , DefaultWeightedEdge>(edgeNum , g.edgeSet().size());
		
		HashSet<MatchedPair<SplitVertex<Integer, DefaultWeightedEdge>>> matching = new HashSet<MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>>();
		
		DefaultWeightedEdge e1 = g.getEdge(1, 2);
		DefaultWeightedEdge e2 = g.getEdge(2, 4);
		DefaultWeightedEdge e3 = g.getEdge(5, 6);
		DefaultWeightedEdge e4 = g.getEdge(3, 5);
		
		MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>> pair = 
			new MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e1) , gPrime.getSplitVertexFromEdge(e2) , 0.5);
		matching.add(pair);
		
		pair = new MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e1) , gPrime.getSplitVertexFromEdge(e3) , 0.5);
		matching.add(pair);
		
		pair = new MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e3) , gPrime.getSplitVertexFromEdge(e4) , 0.5);
		matching.add(pair);
		
		pair = new MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e2) , gPrime.getSplitVertexFromEdge(e4) , 0.5);
		matching.add(pair);

		MatchingMatrix m = matchingStep.computeMatchingMatrix(gPrime, g.edgeSet(), matching);

		SparseDoubleMatrix2D matrix = m.getMatrix();
		
		assertTrue(matrix.getQuick(edgeNum.get(e1), edgeNum.get(e1)) == 0.5);
		assertTrue(matrix.getQuick(edgeNum.get(e2), edgeNum.get(e2)) == 0.5);
		assertTrue(matrix.getQuick(edgeNum.get(e3), edgeNum.get(e3)) == 0.5);
		assertTrue(matrix.getQuick(edgeNum.get(e4), edgeNum.get(e4)) == 0.5);
		
		assertTrue(matrix.getQuick(edgeNum.get(e1), edgeNum.get(e2)) == 0.25);
		assertTrue(matrix.getQuick(edgeNum.get(e2), edgeNum.get(e1)) == 0.25);
		
		assertTrue(matrix.getQuick(edgeNum.get(e1), edgeNum.get(e3)) == 0.25);
		assertTrue(matrix.getQuick(edgeNum.get(e3), edgeNum.get(e1)) == 0.25);
		
		assertTrue(matrix.getQuick(edgeNum.get(e1), edgeNum.get(e4)) == 0.0);
		assertTrue(matrix.getQuick(edgeNum.get(e4), edgeNum.get(e1)) == 0.0);
		
		assertTrue(matrix.getQuick(edgeNum.get(e2), edgeNum.get(e3)) == 0.0);
		assertTrue(matrix.getQuick(edgeNum.get(e2), edgeNum.get(e3)) == 0.0);
		
		assertTrue(matrix.getQuick(edgeNum.get(e2), edgeNum.get(e4)) == 0.25);
		assertTrue(matrix.getQuick(edgeNum.get(e4), edgeNum.get(e2)) == 0.25);
		
		assertTrue(matrix.getQuick(edgeNum.get(e3), edgeNum.get(e4)) == 0.25);
		assertTrue(matrix.getQuick(edgeNum.get(e4), edgeNum.get(e3)) == 0.25);
		
		System.out.println(matrix);
		
	}
	
	@Test
	public void testMultipleMatchingStep() {
		DummyGraphGenerator generator = new DummyGraphGenerator();
		Graph<Integer, DefaultWeightedEdge> g = generator.generateGraph();
		
		SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		Double[][] flowVectors = generator.generateTrivialFlowVectors();
		
		MatchingStep<Integer , DefaultWeightedEdge> matchingStep = new MatchingStep<Integer , DefaultWeightedEdge>(flowVectors , generator.generateEdgeNum() , g.edgeSet().size());
		
		HashSet<MatchedPair<SplitVertex<Integer, DefaultWeightedEdge>>> matching = new HashSet<MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>>();
		
		DefaultWeightedEdge e1 = g.getEdge(1, 2);
		DefaultWeightedEdge e2 = g.getEdge(2, 4);
		DefaultWeightedEdge e3 = g.getEdge(5, 6);
		DefaultWeightedEdge e4 = g.getEdge(3, 5);
		
		MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>> pair = 
			new MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e1) , gPrime.getSplitVertexFromEdge(e2) , 0.5);
		matching.add(pair);
		
		pair = new MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e2) , gPrime.getSplitVertexFromEdge(e3) , 0.5);
		matching.add(pair);
		
		pair = new MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e3) , gPrime.getSplitVertexFromEdge(e4) , 0.5);
		matching.add(pair);
		
		pair = new MatchedPair<SplitVertex<Integer , DefaultWeightedEdge>>(gPrime.getSplitVertexFromEdge(e4) , gPrime.getSplitVertexFromEdge(e1) , 0.5);
		matching.add(pair);
		
		for (int i = 0; i<10 ; i++) {
		
			matchingStep.performMatchingStepV2(gPrime, matching);
			
			for (Double[] vector: flowVectors) {
				Double sum = 0.0;
				for (Double d  :vector) {
					sum += d;
				}
				assertTrue(sum == 1);		
			}
		}
	}
	
}
