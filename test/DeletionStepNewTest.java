package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.DeletionStepNew;
import org.jgrapht.experimental.clustering.FlowProblem;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.UndirectedFlowProblem;
import org.jgrapht.experimental.clustering.old.DeletionStep;
import org.jgrapht.experimental.clustering.old.DummyFlow;
import org.jgrapht.experimental.clustering.old.FlowDecomposer;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class DeletionStepNewTest {

	@Test
	public void testNoMovement() {
		
		//Grid graph
		
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
		
		
		FlowProblem<SplitVertex<Integer , DefaultWeightedEdge>, DefaultWeightedEdge> problem = 
			new UndirectedFlowProblem<SplitVertex<Integer , DefaultWeightedEdge>,DefaultWeightedEdge>(
					gPrime ,
					gPrime.getFlowSource() , 
					gPrime.getFlowTarget());
		
		Integer m = g.edgeSet().size();
		
		DeletionStepNew<Integer, DefaultWeightedEdge> del = new DeletionStepNew<Integer , DefaultWeightedEdge>(g, gPrime, edgeNum,  A.size()); 
		del.computeDeletionMatrix(A, B, A_s, A_t, problem, problem.getFlow());
				
		SparseDoubleMatrix2D matrix = del.getDeletionMatrix().getMatrix();
				
		for (DefaultWeightedEdge e : A) {
			assertTrue(matrix.getQuick(edgeNum.get(e) , edgeNum.get(e)) == 1.0);
		}
	}
	
	@Test
	public void testRestart() {
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
		DefaultWeightedEdge e37 = g.addEdge(3, 7);
		
		DefaultWeightedEdge e34 = g.addEdge(3, 4);
		DefaultWeightedEdge e35 = g.addEdge(3, 5);
		
		DefaultWeightedEdge e45 = g.addEdge(4, 5);
		
		DefaultWeightedEdge e56 = g.addEdge(5, 6);
		DefaultWeightedEdge e57 = g.addEdge(5, 7);
		
		SplitGraph<Integer, DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		SplitVertex<Integer , DefaultWeightedEdge> v;
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();

		A.add(e12);
		A.add(e13);
		
		A.add(e23);
		A.add(e34);
		A.add(e37);
		
		A.add(e56);
		A.add(e57);
		A.add(e45);
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_t = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		
		A_t.add(gPrime.getSplitVertexFromEdge(e12));
		A_t.add(gPrime.getSplitVertexFromEdge(e13));
		A_t.add(gPrime.getSplitVertexFromEdge(e23));
		A_t.add(gPrime.getSplitVertexFromEdge(e34));
		
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_s = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		
//		A_s.add(gPrime.getSplitVertexFromEdge(e56));
//		A_s.add(gPrime.getSplitVertexFromEdge(e57));
		A_s.add(gPrime.getSplitVertexFromEdge(e45));
		
		BiMap<DefaultWeightedEdge , Integer> edgeNum =  HashBiMap.create();
		Integer i = 0;
		for (DefaultWeightedEdge e : g.edgeSet()) {
			edgeNum.put(e, i++);
		}
		
		gPrime.addSourceAndTarget(A_s, A_t);

		
		FlowProblem<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> problem = 
			new UndirectedFlowProblem<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge>(
					gPrime, 
					gPrime.getFlowSource() ,
					gPrime.getFlowTarget());
		
		Integer m = g.edgeSet().size();
			
		
		DeletionStepNew<Integer, DefaultWeightedEdge> del = new DeletionStepNew<Integer , DefaultWeightedEdge>(g, gPrime, edgeNum,  A.size()); 
		del.computeDeletionMatrix(A, B, A_s, A_t, problem, problem.getFlow());
		
		for (DefaultWeightedEdge e : A) {
			assertTrue(del.restartNeccessary());
		}
		
	}
	
	@Test
	public void testMovement() {
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph(5);
		
		
		Set<DefaultWeightedEdge> A = g.edgeSet();
		Set<DefaultWeightedEdge> B = new HashSet<DefaultWeightedEdge>();
		
		SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_s = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_t = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		
		A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(1,2)));
		A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(1,3)));
		A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(1,4)));
		
		A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(8,10)));
		A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(7,10)));
		A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(9,10)));
		A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(10,11)));
		A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(10,12)));
		A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(10,13)));
		
		gPrime.addSourceAndTarget(A_s, A_t);

		FlowProblem<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> problem = 
			new UndirectedFlowProblem<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge>(
					gPrime,
					gPrime.getFlowSource(),
					gPrime.getFlowTarget());
		
		Integer m = g.edgeSet().size();
		

		BiMap<DefaultWeightedEdge , Integer> edgeNum =  HashBiMap.create();
		Integer i = 0;
		for (DefaultWeightedEdge e : g.edgeSet()) {
			edgeNum.put(e, i++);
		}
		
		DeletionStepNew<Integer, DefaultWeightedEdge> del = new DeletionStepNew<Integer , DefaultWeightedEdge>(g, gPrime, edgeNum,  A.size()); 
		del.computeDeletionMatrix(A, B, A_s, A_t, problem, problem.getFlow());
		
		
		for (DefaultWeightedEdge e : A) {
			assertTrue(del.restartNeccessary());
		}
		
		
	}
	
}
