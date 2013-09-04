package org.jgrapht.experimental.clustering;

import java.util.Set;

import cern.colt.matrix.tdouble.DoubleMatrix1D;

/**
 * Interface for a KRV Step (Either deletion or matching) 
 * 
 * @author moritzfuchs
 *
 */
public interface KRVStep<V,E> {

	/**
	 * Applies the KRVStep to the current projection
	 * 
	 * @param r : The current random direction
	 * @param current_projection : The length of the current projections of flow vectors onto the given random direction
	 * @return
	 */
	public DoubleMatrix1D applyStep(DoubleMatrix1D r , DoubleMatrix1D current_projection);
	
	public Set<E> getA();
	
}
