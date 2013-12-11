package org.jgrapht.experimental.clustering.krv;

import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

public class FlowVectorProjector<V,E> {
	

	private DoubleMatrix2D capMatrix;
	
	/**
	 * Initialize capacity matrix 
	 * 
	 * @param g : The original Graph G
	 * @param edgeNum : Map between edges of G and a unique id for each edge
	 */
	public FlowVectorProjector(Graph<V,E> g , Map<E , Integer> edgeNum) {
		capMatrix = new SparseDoubleMatrix2D(g.edgeSet().size(),g.edgeSet().size());
		
		for (E e : g.edgeSet()) {
			capMatrix.setQuick(edgeNum.get(e), edgeNum.get(e), g.getEdgeWeight(e));
		}
	}
	
	/**
	 * Computes the current projection of the flow vectors onto a random direction r
	 * 
	 * @param projection : The vector that the flow vectors will be projected on. 
	 * @return DoubleMatrix1D : The projection of the flow vectors onto the given vector
	 */
	public DoubleMatrix1D getFlowVectorProjection(List<KRVStep<V,E>> matrices , DoubleMatrix1D r) {
		DoubleMatrix1D projection = r.copy();
		
		//First step = set flow vector sizes for different weights of edges in g
		projection = capMatrix.zMult(projection, projection.copy());
		
		
		for (KRVStep<V,E> step : matrices) {
			projection = step.applyStep(projection.copy(), projection);
		}
		
		return projection;
	}
	
}
