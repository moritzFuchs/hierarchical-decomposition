package org.jgrapht.experimental.clustering;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.test.GraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleGraph<Integer , DefaultWeightedEdge> g = new SimpleGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addVertex(4);
		g.addVertex(5);
		g.addVertex(6);
		
		DefaultWeightedEdge e1,e2,e3,e4,e5,e6,e7;
		
		e1 = g.addEdge(1, 2);
		g.setEdgeWeight(e1, 1);
		
		e2 = g.addEdge(1, 3);
		g.setEdgeWeight(e2, 2);
		
		e3 = g.addEdge(2, 4);
		g.setEdgeWeight(e3, 1);
		
		e4 = g.addEdge(3, 4);
		g.setEdgeWeight(e4, 1);
		
		e5 = g.addEdge(3, 5);
		g.setEdgeWeight(e5, 1);
		
		e6 = g.addEdge(4, 6);
		g.setEdgeWeight(e6, 2);
		
		e7 = g.addEdge(5, 6);
		g.setEdgeWeight(e7, 2);
		
		GraphGenerator generator = new GraphGenerator();
		
		Graph<Integer , DefaultWeightedEdge> g2 = generator.generateGraph(2);
		PartitionATask<Integer , DefaultWeightedEdge> task = new PartitionATask<Integer , DefaultWeightedEdge>(g2);
		
		PartitionA<Integer , DefaultWeightedEdge> part = new PartitionA<Integer , DefaultWeightedEdge>(task);
		
		part.performClustering(g2);
		

	}

}
