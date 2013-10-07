package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.krv.DeletionStepNew;
import org.jgrapht.experimental.clustering.old.DummyFlow;
import org.jgrapht.experimental.clustering.old.FlowDecomposer;
import org.jgrapht.experimental.clustering.old.VerticeDivider;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class FlowDecomposerTest {

	/**
	 * Test the extraction of a partial, fractional matching out of a given flow (on the dummy graph of TestUtil)
	 */
	@Test
	public void extractMatchingFromFlow() {
		
		DummyGraphGenerator generator = new DummyGraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph();
		
		for (int i=0;i<10000;i++) {
			
			SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
			
			VerticeDivider<Integer,DefaultWeightedEdge> divider = new VerticeDivider<Integer,DefaultWeightedEdge>(generator.generateTrivialFlowVectors() , g.edgeSet().size() , generator.generateEdgeNum());

			Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>(g.edgeSet());
//			Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
			
			//Divide set of active edges into sets A_s (first element in pair) and A_t (second element in pair)
			divider.divideActiveVertices(gPrime, A );

			gPrime.addSourceAndTarget(divider.getAs() , divider.getAt());
			
			DummyFlow<SplitVertex<Integer , DefaultWeightedEdge>, DefaultWeightedEdge> flow = new DummyFlow<SplitVertex<Integer , DefaultWeightedEdge>, DefaultWeightedEdge>(gPrime , gPrime.getFlowSource() , gPrime.getFlowTarget());
			FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge>,DefaultWeightedEdge> dec = new FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge>,DefaultWeightedEdge>();
			
			Set<MatchedPair<SplitVertex<Integer, DefaultWeightedEdge>>> matching = dec.getFractionalPartialMatching(gPrime, divider.getAs(), divider.getAt(), flow.getFlow(), gPrime.getFlowSource(), gPrime.getFlowTarget());
			
			assertTrue(matching.size() == 3);
		}
	}
	
	@Test
	public void testPathDecomposition() {
		for (int i = 0; i<1000; i++) {
			Graph<Integer , DefaultWeightedEdge> g = TestUtil.getDummyGraph();
			
			SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
			
			Set<SplitVertex<Integer , DefaultWeightedEdge>> A_s = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
			A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(1,2)));
			A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(4,6)));
			A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(5,6)));
			
			Set<SplitVertex<Integer , DefaultWeightedEdge>> A_t = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
			A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(1,3)));
			A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(3,5)));
			A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(3,4)));
			
			gPrime.addSourceAndTarget(A_s, A_t);
			
			DummyFlow<SplitVertex<Integer , DefaultWeightedEdge>, DefaultWeightedEdge> flow = new DummyFlow<SplitVertex<Integer , DefaultWeightedEdge>, DefaultWeightedEdge>(gPrime , gPrime.getFlowSource() , gPrime.getFlowTarget());
			FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge>,DefaultWeightedEdge> dec = new FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge>,DefaultWeightedEdge>();
			
			Set<List<SplitVertex<Integer, DefaultWeightedEdge>>> paths = dec.getFlowPaths(gPrime, flow.getFlow(), gPrime.getFlowSource(), gPrime.getFlowTarget());
			
			System.out.println(paths.size());
			
			assertTrue(paths.size() == 3);
		}
	}
	
	@Test
	public void testCutExtraction() {
		GraphGenerator generator = new GraphGenerator();
		
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph(2);
		
		SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		Set<SplitVertex<Integer, DefaultWeightedEdge>> A_s = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		Set<SplitVertex<Integer, DefaultWeightedEdge>> A_t = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		
		A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(3, 4)));
		A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(1, 3)));
		
		A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(4, 7)));
		A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(2, 4)));
		
		gPrime.addSourceAndTarget(A_s, A_t);
		
		DummyFlow<SplitVertex<Integer, DefaultWeightedEdge>, DefaultWeightedEdge> flow = new DummyFlow<SplitVertex<Integer,DefaultWeightedEdge>, DefaultWeightedEdge>(gPrime , gPrime.getFlowSource() , gPrime.getFlowTarget());
		Map<DefaultWeightedEdge , Double> flowMap = flow.getFlow();
		FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge>,DefaultWeightedEdge> dec = new FlowDecomposer<SplitVertex<Integer, DefaultWeightedEdge>,DefaultWeightedEdge>();
		
		Set<DefaultWeightedEdge> cut = dec.getCut(gPrime, flowMap, gPrime.getFlowSource(),gPrime.getFlowTarget());
				
		assertTrue(cut.size() <= 2);

	}
	
	@Test
	public void testCutExtraction2() {
		SimpleGraph<Integer , DefaultWeightedEdge> g = new SimpleGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		g.addVertex(7);
		
		DefaultWeightedEdge e12 = g.addEdge(1, 2);
		DefaultWeightedEdge e13 = g.addEdge(1, 3);
		DefaultWeightedEdge e23 = g.addEdge(2, 3);
		
		DefaultWeightedEdge e34 = g.addEdge(3, 4);
		DefaultWeightedEdge e35 = g.addEdge(3, 5);
		
		DefaultWeightedEdge e45 = g.addEdge(4, 5);
		
		DefaultWeightedEdge e56 = g.addEdge(5, 6);
		DefaultWeightedEdge e57 = g.addEdge(5, 7);
		
		SplitGraph<Integer, DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		SplitVertex<Integer , DefaultWeightedEdge> v;
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
		A.add(e23);
		
		A.add(e12);
		A.add(e13);
		
		A.add(e23);
		A.add(e34);
		
		A.add(e56);
		A.add(e57);
		A.add(e45);
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_t = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		
		A_t.add(gPrime.getSplitVertexFromEdge(e12));
		A_t.add(gPrime.getSplitVertexFromEdge(e13));
		A_t.add(gPrime.getSplitVertexFromEdge(e23));
//		A_t.add(gPrime.getSplitVertexFromEdge(e34));
		
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_s = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		
		A_s.add(gPrime.getSplitVertexFromEdge(e56));
		A_s.add(gPrime.getSplitVertexFromEdge(e57));
		A_s.add(gPrime.getSplitVertexFromEdge(e45));
		
		BiMap<DefaultWeightedEdge , Integer> edgeNum =  HashBiMap.create();
		Integer i = 0;
		for (DefaultWeightedEdge e : g.edgeSet()) {
			edgeNum.put(e, i++);
		}
		
		gPrime.addSourceAndTarget(A_s, A_t);
		
		DummyFlow<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> flow = 
			new DummyFlow<SplitVertex<Integer , DefaultWeightedEdge>, DefaultWeightedEdge>(gPrime, gPrime.getFlowSource(), gPrime.getFlowTarget());

		FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> dec = new FlowDecomposer<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge>();		

		System.out.println(dec.getCut(gPrime, flow.getFlow(), gPrime.getFlowSource(),gPrime.getFlowTarget()));
		
		assertTrue(dec.getCut(gPrime, flow.getFlow(), gPrime.getFlowSource(),gPrime.getFlowTarget()).size() == 3);
		
	}
	
}
