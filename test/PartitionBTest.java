package org.jgrapht.experimental.clustering.test;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.PartitionB;
import org.jgrapht.experimental.clustering.SubGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;


public class PartitionBTest {

	@Test
	public void simpleTest() {
		GraphGenerator generator = new GraphGenerator();
		Graph<Integer , DefaultWeightedEdge> g = new SimpleGraph<Integer , DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//Generate 4x4 grid
		
		for (int i=1; i<=16;i++) {
			g.addVertex(i);
		}
		
		for (int i=1;i<=16;i++) {
			if (i % 4 != 0) {
				g.addEdge(i, i+1);
			}
			
			if (i+4 <= 16) {
				g.addEdge(i , i+4);
			}
		}
		
		
		
		
		//generate subgraph
		Set<Integer> vertices = new HashSet<Integer>();
		for (int i = 1;i<=10;i++) {
			vertices.add(i);
		}
		
		Graph<Integer , DefaultWeightedEdge> subG = SubGraphGenerator.generateSubGraph(g, vertices);
		
		Set<DefaultWeightedEdge> clustering = new HashSet<DefaultWeightedEdge>(); 
		clustering.add(subG.getEdge(5, 9));
		clustering.add(subG.getEdge(6, 10));
		
		clustering.add(subG.getEdge(2, 3));
		clustering.add(subG.getEdge(6, 7));
		
		
		PartitionB part = new PartitionB<Integer, DefaultWeightedEdge>(g , subG , clustering);
		
//		System.out.println();
		
		
	}
	
}
