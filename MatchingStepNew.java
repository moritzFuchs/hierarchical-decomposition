package org.jgrapht.experimental.clustering;

import java.util.Map;
import java.util.Set;

import org.jgrapht.experimental.clustering.util.MatchedPair;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

/**
 * Contains the algorithm to perform a matching step in the modified efficient KRV procedure.
 * Gets fractional, partition matching computes the corresponding matching matrix 
 * 
 * @author moritzfuchs
 *
 * @param <V>
 * @param <E>
 */
public class MatchingStepNew<V extends Comparable<V>,E> implements KRVStep<V,E> {

	/**
	 * Fraction of weight that is moved with each matching
	 */
	private static final Double FLOW_MOVEMENT_FRACTION = 0.5;
	
	/**
	 * Container for the computed matching matrix
	 */
	private MatchingMatrix matrixContainer;
	
	/**
	 * Mapping of edges to a unique integer \in [0 ... m-1] 
	 */
	private Map<E , Integer> edgeNum;
	
	/**
	 * Number of edges in G 
	 */
	private Integer m;
	
	private Set<E> A;
	
	public MatchingStepNew(Map<E , Integer> edgeNum , Integer m) {
		
		this.edgeNum = edgeNum;
		this.m = m;
	}
	
	
	//TODO: better documentation
	/**
	 * Get the Matching Matrix 
	 * 
	 * @param gPrime : The subdivision graph G' of G
	 * @param activeEdges : The set of currently active edges
	 * @param matching : The set of matched pairs
	 * @return : The sparse matrix corresponding to the given matching
	 */
	public MatchingMatrix computeMatchingMatrix(SplitGraph<V,E> gPrime,
			Set<E> activeEdges,
			Set<MatchedPair<SplitVertex<V, E>>> matching) {
		
		A = activeEdges;
		
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(m,m);
		
		for (E e : activeEdges) {
			matrix.setQuick(edgeNum.get(e), edgeNum.get(e), 1.0);
		}
		
		for (MatchedPair<SplitVertex<V,E>> pair : matching) {
			SplitVertex<V,E> from = pair.getV();
			SplitVertex<V,E> to = pair.getW();
			Double weight = pair.getWeight();
			
			E fromEdge = gPrime.getOriginalEdge(from);
			E toEdge = gPrime.getOriginalEdge(to);
			
			Integer toNum = edgeNum.get(toEdge);
			Integer fromNum = edgeNum.get(fromEdge);
			
			matrix.setQuick(fromNum, toNum, weight * FLOW_MOVEMENT_FRACTION);
			matrix.setQuick(toNum, fromNum, weight * FLOW_MOVEMENT_FRACTION);
			
			matrix.setQuick(toNum, toNum, matrix.getQuick(toNum,toNum) - weight * FLOW_MOVEMENT_FRACTION);
			matrix.setQuick(fromNum, fromNum, matrix.getQuick(fromNum,fromNum) - weight * FLOW_MOVEMENT_FRACTION);
		}
		
		matrixContainer = new MatchingMatrix(matrix);
				
		return matrixContainer;
	}
	
	/**
	 * Returns the computed matrix
	 * 
	 * @return : The computed matrix (or null if the matrix has not yet been computed)
	 */
	public SparseDoubleMatrix2D getMatrix() {
		return matrixContainer.getMatrix();
	}
	
	public Set<E> getA() {
		return A;
	}
	
	@Override
	public DoubleMatrix1D applyStep(DoubleMatrix1D r,
			DoubleMatrix1D current_projection) {
		
		DoubleMatrix1D new_projection = current_projection.copy();
		new_projection = matrixContainer.getMatrix().zMult(current_projection, new_projection);
		
		return new_projection;
		
	}
}
