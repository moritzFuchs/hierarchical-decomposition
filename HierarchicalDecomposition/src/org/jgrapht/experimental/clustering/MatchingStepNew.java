package org.jgrapht.experimental.clustering;

import java.util.Set;

import org.jgrapht.experimental.clustering.util.MatchedPair;

import com.google.common.collect.BiMap;

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
	 * Container for the computed matching matrix
	 */
	private MatchingMatrix matrixContainer;
	
	/**
	 * Mapping of edges to a unique integer \in [0 ... m-1] 
	 */
	private BiMap<E , Integer> edgeNum;
	
	/**
	 * Number of edges in G 
	 */
	private Integer m;
	
	private Set<E> A;
	
	public MatchingStepNew(BiMap<E , Integer> edgeNum , Integer m) {
		
		this.edgeNum = edgeNum;
		this.m = m;
	}
	
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
		
		for (E e : A) {
			matrix.setQuick(edgeNum.get(e), edgeNum.get(e), 1.0);
		}
		
		for (MatchedPair<SplitVertex<V,E>> pair : matching) {
			SplitVertex<V,E> from = pair.getV();
			SplitVertex<V,E> to = pair.getW();
			Double weight = pair.getWeight();
			
			E fromEdge = gPrime.getOriginalEdge(from);
			E toEdge = gPrime.getOriginalEdge(to);
			
			Integer fromNum = edgeNum.get(fromEdge);
			Integer toNum = edgeNum.get(toEdge);
			
			if (DecompositionConstants.DEBUG) {
				debugTests(gPrime, matrix, fromEdge, toEdge, fromNum, toNum);
			}
			
			/*
			 * We need to make sure, that the outgoing flow-vector length is equal to the incoming flow vector length.
			 * Since the weight is the absolute amount of flow we want to move, the matrix entries have to be percentages of the existing flow-vector,
			 * s.t. percentage * edge capacity = weight.
			 */
			
			Double percentage_from = weight / gPrime.getOriginalGraph().getEdgeWeight(fromEdge);
			Double percentage_to = weight / gPrime.getOriginalGraph().getEdgeWeight(toEdge);

			matrix.setQuick(fromNum, toNum, matrix.getQuick(fromNum, toNum) + percentage_to * DecompositionConstants.FLOW_MOVEMENT_FRACTION);
			matrix.setQuick(toNum, fromNum, matrix.getQuick(toNum, fromNum) + percentage_from * DecompositionConstants.FLOW_MOVEMENT_FRACTION);
			
			matrix.setQuick(fromNum, fromNum, matrix.getQuick(fromNum,fromNum) - percentage_from * DecompositionConstants.FLOW_MOVEMENT_FRACTION);
			matrix.setQuick(toNum, toNum, matrix.getQuick(toNum,toNum) - percentage_to * DecompositionConstants.FLOW_MOVEMENT_FRACTION);
			
			if (DecompositionConstants.DEBUG) {
				debugTests(gPrime, matrix, fromEdge, toEdge, fromNum, toNum);
			}
			
		}
		
		matrixContainer = new MatchingMatrix(matrix);
				
		return matrixContainer;
	}


	/**
	 * Some tests for debugging..
	 * 
	 * @param gPrime
	 * @param matrix
	 * @param fromEdge
	 * @param toEdge
	 * @param fromNum
	 * @param toNum
	 */
	private void debugTests(SplitGraph<V, E> gPrime,
			SparseDoubleMatrix2D matrix, E fromEdge, E toEdge, Integer fromNum,
			Integer toNum) {
		
		Double epsilon = 0.0000001;
		
		DoubleMatrix1D row = matrix.viewRow(edgeNum.get(fromEdge));
		Double sum = 0.0;
		for (int j=0;j<row.size();j++) {
			sum += row.get(j) * gPrime.getOriginalGraph().getEdgeWeight(edgeNum.inverse().get(j));
		}
		if (sum <= gPrime.getOriginalGraph().getEdgeWeight(fromEdge) - epsilon || sum >= gPrime.getOriginalGraph().getEdgeWeight(fromEdge) + epsilon ) {
			System.out.println("From edge fail (after) " + fromNum);
		}
		
		row = matrix.viewRow(edgeNum.get(toEdge));
		sum = 0.0;
		for (int j=0;j<row.size();j++) {
			sum += row.get(j) * gPrime.getOriginalGraph().getEdgeWeight(edgeNum.inverse().get(j));
		}
		if (sum <= gPrime.getOriginalGraph().getEdgeWeight(toEdge) - epsilon || sum >= gPrime.getOriginalGraph().getEdgeWeight(toEdge) + epsilon ) {
			System.out.println("To edge fail (after) " + toNum);
		}
		
		DoubleMatrix1D col = matrix.viewColumn(edgeNum.get(fromEdge));
		sum = 0.0;
		for (int j=0;j<col.size();j++) {
			sum += col.get(j);
		}
		
		if ((sum >= 1.0 + epsilon || sum <= 1.0 - epsilon) && sum != 0.0) {
			System.out.println("Col " + fromNum);
		}
	}
	
	/**
	 * Returns the computed matrix.
	 * 
	 * @return : The computed matrix (or null if the matrix has not yet been computed)
	 */
	public SparseDoubleMatrix2D getMatrix() {
		return matrixContainer.getMatrix();
	}
	
	/**
	 * Returns the set of active edges.
	 * 
	 * @return Set<E> : The set of active edges.
	 */
	public Set<E> getA() {
		return A;
	}
	
	@Override
	public DoubleMatrix1D applyStep(DoubleMatrix1D r,
			DoubleMatrix1D current_projection) {
		
		DoubleMatrix1D new_projection = current_projection.copy();
		
		for (int i=0;i<DecompositionConstants.MATCHING_APPLICATIONS;i++) {
			new_projection = matrixContainer.getMatrix().zMult(new_projection.copy(), new_projection);
		}
		
		return new_projection;
		
	}
}
