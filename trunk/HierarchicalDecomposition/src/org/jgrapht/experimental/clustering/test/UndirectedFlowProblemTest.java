package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.clustering.FlowPath;
import org.jgrapht.experimental.clustering.FlowProblem;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.UndirectedFlowProblem;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Test;


public class UndirectedFlowProblemTest {

	
	@Test
	public void simpleFlowTest() {
		DummyGraphGenerator generator = new DummyGraphGenerator();
		DirectedGraph<Integer , DefaultWeightedEdge> g = generator.generateDirectedGraph();
		
		UndirectedFlowProblem<Integer , DefaultWeightedEdge> problem = new UndirectedFlowProblem<Integer , DefaultWeightedEdge>(g , 1 , 6);
		
		Map<DefaultWeightedEdge , Double> flow = problem.getFlow();
		
		assertTrue(flow.get(g.getEdge(1, 2)) == 1.0);
		assertTrue(flow.get(g.getEdge(1, 3)) == 2.0);
		assertTrue(flow.get(g.getEdge(2, 4)) == 1.0);
		assertTrue(flow.get(g.getEdge(3, 4)) == 1.0);
		assertTrue(flow.get(g.getEdge(3, 5)) == 1.0);
		assertTrue(flow.get(g.getEdge(4, 6)) == 2.0);
		assertTrue(flow.get(g.getEdge(5, 6)) == 1.0);
		
		System.out.println(flow);
	}
	
	@Test
	public void simpleCutTest() {
		DummyGraphGenerator generator = new DummyGraphGenerator();
		DirectedGraph<Integer , DefaultWeightedEdge> g = generator.generateDirectedGraph();
		
		UndirectedFlowProblem<Integer , DefaultWeightedEdge> problem = new UndirectedFlowProblem<Integer , DefaultWeightedEdge>(g , 1 , 6);
		
		Set<DefaultWeightedEdge> cut = problem.getCut();
		
		System.out.println(cut);
		
		assertTrue(cut.size() == 2);
	}
	
	@Test
	public void simpleFlowAndPathTest() {
		DummyGraphGenerator generator = new DummyGraphGenerator();
		DirectedGraph<Integer , DefaultWeightedEdge> g = generator.generateDirectedGraph();
		
		UndirectedFlowProblem<Integer , DefaultWeightedEdge> problem = new UndirectedFlowProblem<Integer , DefaultWeightedEdge>(g , 1 , 6);
		
		Map<DefaultWeightedEdge , Double> flow = problem.getFlow();
		
		assertTrue(flow.get(g.getEdge(1, 2)) == 1.0);
		assertTrue(flow.get(g.getEdge(1, 3)) == 2.0);
		assertTrue(flow.get(g.getEdge(2, 4)) == 1.0);
		assertTrue(flow.get(g.getEdge(3, 4)) == 1.0);
		assertTrue(flow.get(g.getEdge(3, 5)) == 1.0);
		assertTrue(flow.get(g.getEdge(4, 6)) == 2.0);
		assertTrue(flow.get(g.getEdge(5, 6)) == 1.0);
		
		System.out.println(problem.getPaths());
		
		assertTrue(problem.getPaths().size() == 3);
		
		
		for (FlowPath<Integer,DefaultWeightedEdge> path : problem.getPaths()) {
			System.out.println(path + " " + problem.getFlowPathWeight(path));
			assertTrue(problem.getFlowPathWeight(path) == 1.0);
		}
		
		assertTrue(flow.get(g.getEdge(1, 2)) == 1.0);
		assertTrue(flow.get(g.getEdge(1, 3)) == 2.0);
		assertTrue(flow.get(g.getEdge(2, 4)) == 1.0);
		assertTrue(flow.get(g.getEdge(3, 4)) == 1.0);
		assertTrue(flow.get(g.getEdge(3, 5)) == 1.0);
		assertTrue(flow.get(g.getEdge(4, 6)) == 2.0);
		assertTrue(flow.get(g.getEdge(5, 6)) == 1.0);
	}
	
	@Test
	public void negativeFlowPathWeight() {
		SimpleWeightedGraph<Integer, DefaultWeightedEdge> g = new SimpleWeightedGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		
		DefaultWeightedEdge e12 = g.addEdge(1, 2);
		DefaultWeightedEdge e13 = g.addEdge(1, 3);
		DefaultWeightedEdge e23 = g.addEdge(2, 3);
		DefaultWeightedEdge e24 = g.addEdge(2, 4);
		
		g.setEdgeWeight(e12, 1.0);
		g.setEdgeWeight(e13, 1.0);
		g.setEdgeWeight(e23, 1.0);
		g.setEdgeWeight(e24, 2.0);
		
		UndirectedFlowProblem<Integer , DefaultWeightedEdge> problem = new UndirectedFlowProblem<Integer , DefaultWeightedEdge>(g , 1 , 4);
		
		Map<DefaultWeightedEdge, Double> flow = problem.getFlow();
		assertTrue(flow.get(e12) == 1.0);
		assertTrue(flow.get(e13) == 1.0);
		assertTrue(flow.get(e23) == -1.0);
		assertTrue(flow.get(e24) == 2.0);

		Set<FlowPath<Integer,DefaultWeightedEdge>> paths = problem.getPaths();
		
		System.out.println(paths);
		
		assertTrue(paths.size() == 2);
		
		for (FlowPath<Integer, DefaultWeightedEdge> path : paths) {
			System.out.println(problem.getFlowPathWeight(path));
			assertTrue(problem.getFlowPathWeight(path) == 1.0);
		}
		
	}
	
	@Test
	public void simpleCutTest2() {
		SimpleWeightedGraph<Integer, DefaultWeightedEdge> g = new SimpleWeightedGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
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
		DefaultWeightedEdge e24 = g.addEdge(2, 4);
		
		DefaultWeightedEdge e51 = g.addEdge(5, 1);
		
		DefaultWeightedEdge e26 = g.addEdge(2, 6);
		DefaultWeightedEdge e27 = g.addEdge(2, 7);
		DefaultWeightedEdge e67 = g.addEdge(6, 7);
		
		g.setEdgeWeight(e12, 100.3);
		g.setEdgeWeight(e13, 7.5);
		g.setEdgeWeight(e23, 5.0);
		g.setEdgeWeight(e24, 2000.0);
		
		g.setEdgeWeight(e51, 20.5);
		
		g.setEdgeWeight(e26, 15.0);
		g.setEdgeWeight(e27, 10.0);
		g.setEdgeWeight(e67, 10.0);
				
		UndirectedFlowProblem<Integer , DefaultWeightedEdge> problem = new UndirectedFlowProblem<Integer , DefaultWeightedEdge>(g , 5 , 7);
		
		Set<FlowPath<Integer,DefaultWeightedEdge>> paths = problem.getPaths();
		
		assertTrue(paths.size() == 2);
		
		Set<DefaultWeightedEdge> cut = problem.getCut();
		
		assertTrue(cut.contains(g.getEdge(2, 7)));
		assertTrue(cut.contains(g.getEdge(6, 7)));
		
		
		System.out.println(problem.getPaths());
		
		
	}
	
	@Test
	public void simpleSplitGraphTest() {
		SimpleWeightedGraph<Integer, DefaultWeightedEdge> g = new SimpleWeightedGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		g.addVertex(7);
		
		DefaultWeightedEdge e12 = g.addEdge(1, 2);
		DefaultWeightedEdge e23 = g.addEdge(2, 3);
		DefaultWeightedEdge e34 = g.addEdge(3, 4);
		DefaultWeightedEdge e35 = g.addEdge(3, 5);
		
		DefaultWeightedEdge e45 = g.addEdge(4, 5);
		DefaultWeightedEdge e46 = g.addEdge(4, 6);
		
		DefaultWeightedEdge e56 = g.addEdge(5, 6);
		
		DefaultWeightedEdge e67 = g.addEdge(6, 7);
		
		g.setEdgeWeight(e12, 10.7);
		g.setEdgeWeight(e23, 8.5);
		g.setEdgeWeight(e34, 5.0);
		g.setEdgeWeight(e35, 3.5);
		
		g.setEdgeWeight(e45, 0.5);
		
		g.setEdgeWeight(e46, 4.5);
		g.setEdgeWeight(e56, 4.0);
		g.setEdgeWeight(e67, 7.0);
		
		SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_s = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		A_s.add(gPrime.getSplitVertexFromEdge(e12));
		
		Set<SplitVertex<Integer , DefaultWeightedEdge>> A_t = new HashSet<SplitVertex<Integer , DefaultWeightedEdge>>();
		A_t.add(gPrime.getSplitVertexFromEdge(e67));
		A_t.add(gPrime.getSplitVertexFromEdge(e56));
		
		gPrime.addSourceAndTarget(A_s, A_t);
				
		UndirectedFlowProblem<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> problem = new UndirectedFlowProblem<SplitVertex<Integer, DefaultWeightedEdge > , DefaultWeightedEdge>(gPrime , gPrime.getFlowSource() , gPrime.getFlowTarget());
		
		Set<DefaultWeightedEdge> cut = problem.getCut();
		
		assertTrue(cut.size() == 2);
		assertTrue(problem.getPaths().size() == 2);
		
	}
	
}
