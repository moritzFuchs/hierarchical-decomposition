package org.jgrapht.experimental.clustering.old;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class VectorPotential<V,E> {
	
	/**
	 * Map from edges in G to Integer (unique for each edge)
	 */
	private Map<E, Integer> edgeNum;
	
	public VectorPotential(Map<E , Integer> edgeNum) {
		this.edgeNum = edgeNum;
	}
	
	/**
	 * Compute potential according to 'paper' page 6 bottom
	 * 
	 * @return potential of the current flow vectors
	 */
	public Double getPotential(Double[][] vectors , Set<E> A) {
		
		Double[] avg = computeAverageFlowVector(vectors , A);
		Double potential = 0.0;
		
		//compute potential (paper page 6, bottom)
		for (E e: A) {
			for ( int i=0; i < vectors[edgeNum.get(e)].length; i++ ) {
				potential += Math.pow(vectors[edgeNum.get(e)][i] - avg[i], 2);	
			}
		}
		return potential;
	}

	/**
	 * Computes the average over all flow vectors
	 *
	 * @return the current average flow vector
	 */
	private Double[] computeAverageFlowVector(Double[][] vectors , Set<E> activeEdges) {
		
		Integer m = vectors.length;
		
		Double[] avg = new Double[m];
		Arrays.fill(avg, 0.0);
		
		//Sum up flow vectors
		for (E e: activeEdges) {
			for (int i= 0; i<m; i++) {				
				avg[i] += vectors[edgeNum.get(e)][i];
			} 
		}
		
		//average flow vectors
		for (int i = 0;i<m;i++) {
			avg[i] = avg[i] / activeEdges.size();
		}

		return avg;
	}
	
}
