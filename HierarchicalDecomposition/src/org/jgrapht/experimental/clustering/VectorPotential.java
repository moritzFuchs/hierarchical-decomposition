package org.jgrapht.experimental.clustering;

import java.util.Map;
import java.util.Set;

import cern.colt.matrix.tdouble.DoubleMatrix1D;

public class VectorPotential<V,E> {
	
	/**
	 * Map from edges in G to Integer (unique for each edge)
	 */
	private Map<E, Integer> edgeNum;
	
	public VectorPotential(Map<E , Integer> edgeNum) {
		this.edgeNum = edgeNum;
	}
	
	/**
	 * Compute the projection potential
	 * 
	 * @return potential of the projection of flow vectors onto a random direction
	 */
	public Double getPotential(DoubleMatrix1D projection , Set<E> A) {
		
		Double avg = computeAverageProjection(projection , A);
		Double potential = 0.0;
		
		//compute potential (paper page 6, bottom)
		for (E e: A) {
			potential += Math.pow(projection.getQuick(edgeNum.get(e)) - avg, 2);	
		}
		
		return potential;
	}

	/**
	 * Get the average flow vector projection
	 * 
	 * @param projection : The current projection of flow vectors onto a random direction
	 * @param A : The set of active edges (only they have flow vectors)
	 * @return : The average flow vector projection
	 */
	public Double computeAverageProjection(DoubleMatrix1D projection , Set<E> A) {
		Double avg = 0.0;
		for (E e : A) {
			avg += projection.getQuick(edgeNum.get(e));
		}
		return avg / A.size();
	}

}
