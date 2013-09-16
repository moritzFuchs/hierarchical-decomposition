package org.jgrapht.experimental.clustering;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class FlowVectorProjector {

	/**
	 * Computes the current projection of the flow vectors onto a random direction r
	 * 
	 * @param projection : The vector that the flow vectors will be projected on. 
	 * @return DoubleMatrix1D : The projection of the flow vectors onto the given vector
	 */
	public static <V,E> DoubleMatrix1D getFlowVectorProjection(Graph<V,E> g , Set<E> active, Map<E , Integer>edgeNum, List<KRVStep<V,E>> matrices , DoubleMatrix1D r) {
		
		DoubleMatrix1D projection = r.copy();
		
		DoubleMatrix2D cap_matrix = new SparseDoubleMatrix2D(g.edgeSet().size() , g.edgeSet().size());
		
		for (E e : active) {
			cap_matrix.setQuick(edgeNum.get(e), edgeNum.get(e), g.getEdgeWeight(e));
		}
		
		projection = cap_matrix.zMult(projection, projection.copy());
		
		for (KRVStep<V,E> step : matrices) {
			projection = step.applyStep(projection.copy(), projection);
		}
		
		return projection;
	}
	
}
