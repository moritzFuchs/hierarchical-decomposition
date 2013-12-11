package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.old.VectorPotential;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class VectorPotentialTest {

	@Test
	public void dummyGraphSimpleTest() {
		
		DummyGraphGenerator generator = new DummyGraphGenerator();
		Graph<Integer,DefaultWeightedEdge> g = generator.generateGraph();
		
		VectorPotential<Integer , DefaultWeightedEdge> potential = new VectorPotential<Integer , DefaultWeightedEdge>( generator.generateEdgeNum() );
		
		System.out.println(potential.getPotential(generator.generateTrivialFlowVectors(), g.edgeSet()));
		assertTrue(potential.getPotential(generator.generateTrivialFlowVectors(), g.edgeSet()) < 1.1 * 6.0);
		assertTrue(potential.getPotential(generator.generateTrivialFlowVectors(), g.edgeSet()) > 0.9 * 6.0);
	}
	
	@Test
	public void bigGraphSimpleTest() {
		
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer,DefaultWeightedEdge> g = generator.generateGraph(200);
		
		VectorPotential<Integer , DefaultWeightedEdge> potential = new VectorPotential<Integer , DefaultWeightedEdge>( generator.generateEdgeNum() );
		
		System.out.println(potential.getPotential(generator.generateTrivialFlowVectors(), g.edgeSet()));
		assertTrue(potential.getPotential(generator.generateTrivialFlowVectors(), g.edgeSet()) < 1.1 * g.edgeSet().size()-1);
		assertTrue(potential.getPotential(generator.generateTrivialFlowVectors(), g.edgeSet()) > 0.9 * g.edgeSet().size()-1);
	}
	
	@Test
	public void bigGraphTestZero() {
		
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer,DefaultWeightedEdge> g = generator.generateGraph(200);
		
		Map<DefaultWeightedEdge, Integer> edgeNum = generator.generateEdgeNum();
		
		VectorPotential<Integer , DefaultWeightedEdge> potential = new VectorPotential<Integer , DefaultWeightedEdge>( edgeNum );
		
		
		Double[][] flowVectors = new Double[g.edgeSet().size()][g.edgeSet().size()];
		
		for (int i=0;i<g.edgeSet().size(); i++ ) {
			Arrays.fill(flowVectors[i] , 0.0);
		}
		
		flowVectors[0][0] = 1.0/3.0;
		flowVectors[0][1] = 1.0/3.0;
		flowVectors[0][2] = 1.0/3.0;
		
		flowVectors[1][0] = 1.0/3.0;
		flowVectors[1][1] = 1.0/3.0;
		flowVectors[1][2] = 1.0/3.0;
		
		flowVectors[2][0] = 1.0/3.0;
		flowVectors[2][1] = 1.0/3.0;
		flowVectors[2][2] = 1.0/3.0;
		
		
		BiMap<DefaultWeightedEdge , Integer> edgeNumBi = HashBiMap.create(edgeNum); 
		
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		A .add(edgeNumBi.inverse().get(0));
		A.add(edgeNumBi.inverse().get(1));
		A.add(edgeNumBi.inverse().get(2));
		
		System.out.println(potential.getPotential(flowVectors, A));
		
		
		assertTrue(potential.getPotential(flowVectors, A) == 0.0);
	}
	
	@Test
	public void bigGraphTestOneStep() {
		
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer,DefaultWeightedEdge> g = generator.generateGraph(200);
		
		Map<DefaultWeightedEdge, Integer> edgeNum = generator.generateEdgeNum();
		
		VectorPotential<Integer , DefaultWeightedEdge> potential = new VectorPotential<Integer , DefaultWeightedEdge>( edgeNum );
		
		
		Double[][] flowVectors = new Double[g.edgeSet().size()][g.edgeSet().size()];
		
		for (int i=0;i<g.edgeSet().size(); i++ ) {
			Arrays.fill(flowVectors[i] , 0.0);
		}
		
		flowVectors[0][0] = 0.25;
		flowVectors[0][1] = 0.75;
		
		flowVectors[1][0] = 0.75;
		flowVectors[1][1] = 0.25;
		
		flowVectors[2][2] = 0.25;
		flowVectors[2][3] = 0.75;
		
		flowVectors[3][2] = 0.75;
		flowVectors[3][3] = 0.25;
		
		BiMap<DefaultWeightedEdge , Integer> edgeNumBi = HashBiMap.create(edgeNum); 
		
		
		Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>();
		A .add(edgeNumBi.inverse().get(0));
		A.add(edgeNumBi.inverse().get(1));
		A.add(edgeNumBi.inverse().get(2));
		A.add(edgeNumBi.inverse().get(3));
		
		System.out.println(potential.getPotential(flowVectors, A));

		assertTrue(potential.getPotential(flowVectors, A) == 1.5);
	}	
}
