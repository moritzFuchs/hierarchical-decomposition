package org.jgrapht.experimental.clustering;

import java.util.List;

import cern.colt.matrix.tdouble.DoubleMatrix1D;

public class FlowVectorProjector {

	/**
	 * Computes the current projection of the flow vectors onto a random direction r
	 * 
	 * @param projection : The vector that the flow vectors will be projected on. 
	 * @return DoubleMatrix1D : The projection of the flow vectors onto the given vector
	 */
	public static <V,E> DoubleMatrix1D getFlowVectorProjection(List<KRVStep<V,E>> matrices , DoubleMatrix1D r) {
		
		for (KRVStep<V,E> step : matrices) {
			r = step.applyStep(r.copy(), r);
		}
		
		return r;
	}
	
}
