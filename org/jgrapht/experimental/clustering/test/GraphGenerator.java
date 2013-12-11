package org.jgrapht.experimental.clustering.test;

import java.util.HashSet;

import org.jgrapht.Graph;
import org.jgrapht.experimental.clustering.SplitVertex;
import org.jgrapht.experimental.clustering.util.MatchedPair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * Generates a Graph for testing purposes
 * 
 * @author moritzfuchs
 *
 */
public class GraphGenerator extends AbstractGraphGenerator {


	public GraphGenerator() {
		
	}

	/**
	 * Generate a Graph with 1 + 3 * level vertices and 5 * level vertices 
	 * 
	 * @param level
	 * @return
	 */
	public Graph<Integer, DefaultWeightedEdge> generateGraph(Integer level) {
		
		g = new SimpleGraph<Integer, DefaultWeightedEdge>(DefaultWeightedEdge.class);

		g.addVertex(1);
		
		Integer max = 1;
		
		for (int i=0;i<level;i++) {
			g.addVertex(max+1);
			g.addVertex(max+2);
			g.addVertex(max+3);
			
			g.addEdge(max, max+1);
			g.addEdge(max, max+2);
			g.addEdge(max, max+3);
			
			g.addEdge(max+1 , max+3);
			g.addEdge(max+2 , max+3);
			
			max = max+3;
		}

		return g;
	}
		
	
}
