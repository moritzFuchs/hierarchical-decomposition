package org.jgrapht.experimental.decomposition.test;



import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.test.DummyGraphGenerator;
import org.jgrapht.experimental.clustering.test.GraphGenerator;
import org.jgrapht.experimental.decomposition.Decomposition;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;


public class DecompositionTest {


	@Test
	public void simpleTest() {
		DummyGraphGenerator generator = new DummyGraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph();
		
		Decomposition<Integer , DefaultWeightedEdge> dec = new Decomposition<Integer , DefaultWeightedEdge>(g);
		dec.performDecomposition();
		
	}
	
	@Test
	public void smallGraphTest() {
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph(5);
		
		Decomposition<Integer , DefaultWeightedEdge> dec = new Decomposition<Integer , DefaultWeightedEdge>(g);
		dec.performDecomposition();
		
	}
	
	@Test
	public void mediumGraphTest() {
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph(50);
		
		Decomposition<Integer , DefaultWeightedEdge> dec = new Decomposition<Integer , DefaultWeightedEdge>(g);
		dec.performDecomposition();
	}
	
	@Test
	public void bigGraphTest() {
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = generator.generateGraph(500);
		
		Decomposition<Integer , DefaultWeightedEdge> dec = new Decomposition<Integer , DefaultWeightedEdge>(g);
		dec.performDecomposition();
	}
}
