package org.jgrapht.experimental.clustering.test;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

public class TestUtil {

	/**
	 * Get dummy graph for testing with vertex set {1, ... , 6} and edges(from,to,weight) (1,2,1),(1,3,2),(2,4,1),(3,4,1),(3,5,1),(4,6,2),(5,6,2)
	 * 
	 * @return : Dummy graph as specified in description
	 */
	public static Graph<Integer,DefaultWeightedEdge> getDummyGraph() {
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
		
		return g;
	}
	
	/**
	 * Returns the initial flow vectors for a given number of edges (Identity Matrix I_m)
	 * 
	 * @param m : The number of edges (=dimension of vectors)
	 * @return : The initial flow vector for m edges
	 */
	public static Double[][] getInitialFlowVectors(Integer m) {
		Double[][] flowVectors = new Double[m][m];
		
		for (int i=0;i<m;i++) {
			for (int j=0;j<m;j++) {
				if (i == j)
					flowVectors[i][j] = 1.0;
				else
					flowVectors[i][j] = 0.0;
				
			}
		}

		return flowVectors;
	}
	
}
