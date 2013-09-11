package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.experimental.clustering.FlowPath;
import org.jgrapht.experimental.clustering.FlowProblem;
import org.jgrapht.experimental.clustering.UndirectedFlowProblem;
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
		
		FlowProblem<Integer , DefaultWeightedEdge> problem = new UndirectedFlowProblem<Integer , DefaultWeightedEdge>(g , 1 , 4);
		
		Map<DefaultWeightedEdge, Double> flow = problem.getFlow();
		assertTrue(flow.get(e12) == 1.0);
		assertTrue(flow.get(e13) == 1.0);
		assertTrue(flow.get(e23) == -1.0);
		assertTrue(flow.get(e24) == 2.0);

		Set<FlowPath<Integer,DefaultWeightedEdge>> paths = problem.getPaths();
		
		assertTrue(paths.size() == 2);
		
		for (FlowPath<Integer, DefaultWeightedEdge> path : paths) {
			System.out.println(problem.getFlowPathWeight(path));
			assertTrue(problem.getFlowPathWeight(path) == 1.0);
		}
		
	}
	
}
