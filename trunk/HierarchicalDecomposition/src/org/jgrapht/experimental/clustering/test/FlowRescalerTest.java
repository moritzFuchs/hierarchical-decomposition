package org.jgrapht.experimental.clustering.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.FlowPath;
import org.jgrapht.experimental.clustering.FlowProblem;
import org.jgrapht.experimental.clustering.FlowRescaler;
import org.jgrapht.experimental.clustering.PracticalVerticeDivider;
import org.jgrapht.experimental.clustering.SplitGraph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.UndirectedFlowProblem;
import org.jgrapht.experimental.clustering.Util;
import org.jgrapht.experimental.clustering.old.DummyFlow;
import org.jgrapht.experimental.clustering.old.FlowDecomposer;
import org.jgrapht.experimental.clustering.old.VerticeDivider;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

public class FlowRescalerTest {

	@Test
	public void rescaleSimpleFlow() {
		for (int i=0;i<10000;i++) {
			//SETUP -----------------------------------------------
			DummyGraphGenerator generator = new DummyGraphGenerator();
			SimpleGraph<Integer , DefaultWeightedEdge> g = (SimpleGraph<Integer, DefaultWeightedEdge>) generator.generateGraph();

			SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);
	
			Integer m = g.edgeSet().size();
	
			Map<DefaultWeightedEdge , Integer> edgeNum = generator.generateEdgeNum();
			
			DenseDoubleMatrix1D projection = Util.getRandomDirection(m);
			
			PracticalVerticeDivider<Integer , DefaultWeightedEdge> divider = new PracticalVerticeDivider<Integer, DefaultWeightedEdge>(projection , edgeNum);
	
			Set<DefaultWeightedEdge> A = new HashSet<DefaultWeightedEdge>(g.edgeSet());
			
			divider.divideActiveVertices( gPrime, A ,projection);

			//Add source s and target t to G' and connect them to A_s and A_t respectively
			gPrime.addSourceAndTarget(divider.getAs(), divider.getAt());
			
			//Create the flow object
			FlowProblem<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> problem = new UndirectedFlowProblem<SplitVertex<Integer , DefaultWeightedEdge > , DefaultWeightedEdge>(
					gPrime,
					gPrime.getFlowSource(),
					gPrime.getFlowTarget());
			
			//Compute maxFlow
			Map <DefaultWeightedEdge , Double> maxFlow = problem.getMaxFlow();
			
			
			
			// TEST START-----------------------------------------------
			
			//Rescale flow
			FlowRescaler<Integer,DefaultWeightedEdge> rescaler = new FlowRescaler<Integer, DefaultWeightedEdge>();
			Set<FlowPath<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge>> paths = rescaler.rescaleFlow(gPrime, maxFlow, problem);
	
			Map<DefaultWeightedEdge , Double> weight_sum = new HashMap<DefaultWeightedEdge , Double>();
			
			//All flow needs to have value 1 or < 0.5
			for (FlowPath<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> path : paths) {
				SplitVertex<Integer , DefaultWeightedEdge> s = path.getPath().get(0);
				SplitVertex<Integer , DefaultWeightedEdge> x_e = path.getPath().get(1);
				DefaultWeightedEdge e = gPrime.getEdge(s, x_e);
				
				Double weight;
				if (weight_sum.get(e) == null) {
					weight = 0.0;
				} else {
					weight = weight_sum.get(e);
				}

				weight += path.getFlowPathWeight();
				weight_sum.put(e , weight);
			}
			
			for (DefaultWeightedEdge e : weight_sum.keySet()) {
				assertTrue(weight_sum.get(e) == 1.0 || weight_sum.get(e) < 0.5);
			}
		}
	}
	
	@Test
	public void testRescale() {
		//Here Paths will have to be rescaled for sure.
		
		for (int i=0;i<10000;i++) {
			//SETUP -----------------------------------------------
			DummyGraphGenerator generator = new DummyGraphGenerator();
			SimpleGraph<Integer , DefaultWeightedEdge> g = (SimpleGraph<Integer, DefaultWeightedEdge>) generator.generateGraph();

			SplitGraph<Integer , DefaultWeightedEdge> gPrime = new SplitGraph<Integer , DefaultWeightedEdge>(g);

			HashSet<SplitVertex<Integer, DefaultWeightedEdge>> A_s = new HashSet<SplitVertex<Integer,DefaultWeightedEdge>>();
			HashSet<SplitVertex<Integer, DefaultWeightedEdge>> A_t = new HashSet<SplitVertex<Integer,DefaultWeightedEdge>>();

			A_s.add(gPrime.getSplitVertexFromEdge(g.getEdge(1,2)));
			A_t.add(gPrime.getSplitVertexFromEdge(g.getEdge(5,6)));
			
			
			//Add source s and target t to G' and connect them to A_s and A_t respectively
			gPrime.addSourceAndTarget(A_s, A_t);
			
			//Create the flow object
			FlowProblem<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> problem = new UndirectedFlowProblem<SplitVertex<Integer , DefaultWeightedEdge > , DefaultWeightedEdge>(
					gPrime,
					gPrime.getFlowSource(),
					gPrime.getFlowTarget());
			
			//Compute maxFlow
			Map <DefaultWeightedEdge , Double> maxFlow = problem.getMaxFlow();
					
			// TEST START-----------------------------------------------
			
			//Rescale flow
			FlowRescaler<Integer,DefaultWeightedEdge> rescaler = new FlowRescaler<Integer, DefaultWeightedEdge>();
			Set<FlowPath<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge>> paths = rescaler.rescaleFlow(gPrime, maxFlow, problem);
	
			
			//All flow needs to have value 1 or < 0.5
			for (FlowPath<SplitVertex<Integer , DefaultWeightedEdge> , DefaultWeightedEdge> path : paths) {
				assertTrue(path.getFlowPathWeight() == 1.0);
			}
		}
	}
}
