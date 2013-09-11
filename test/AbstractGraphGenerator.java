package org.jgrapht.experimental.clustering.test;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public abstract class AbstractGraphGenerator {

	protected SimpleGraph<Integer , DefaultWeightedEdge> g;
	
	/**
	 * Generates a mapping from edges to Integers
	 * @return
	 */
	public BiMap<DefaultWeightedEdge , Integer> generateEdgeNum() {
		BiMap<DefaultWeightedEdge , Integer> edgeNum = HashBiMap.create(); 
		Integer nextNum = 0;
		for (DefaultWeightedEdge e : g.edgeSet()) {
			edgeNum.put(e , nextNum++);
		}
		return edgeNum;
	}
	
	/**
	 * Generates trivial starting state of flow vectors = I_m (identity matrix in m dimensions
	 * @return
	 */
	public Double[][] generateTrivialFlowVectors() {
		
		Double[][] flowVectors = new Double[g.edgeSet().size()][g.edgeSet().size()];
		
		for (int i=0;i<g.edgeSet().size();i++) {
			for (int j=0;j<g.edgeSet().size();j++) {
				if (i == j)
					flowVectors[i][j] = 1.0;
				else
					flowVectors[i][j] = 0.0;
				
			}
		}

		return flowVectors;
	}
	
}
